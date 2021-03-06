package ru.nsu.ccfit.kokunina;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardOpenOption.*;

public class ClientConnection implements Runnable {
    final private int BUFF_SIZE = 256;
    final private int MEASURE_PERIOD_SEC = 3; // in seconds

    final private String uploadPath;
    final private Socket socket;

    // for measuring speed
    private long fileSizeBytes;
    private final AtomicLong transferredBytes = new AtomicLong(0);
    private final AtomicLong transferTimeMillis = new AtomicLong(0);

    public ClientConnection(Socket socket, String uploadPath) {
        this.socket = socket;
        this.uploadPath = uploadPath;
    }

    @Override
    public void run() {
        ScheduledExecutorService speedMeasurer = Executors.newSingleThreadScheduledExecutor();
        OutputStream newFileStream = null;
        Path newFile = null;
        try {
            InputStream socketStream = socket.getInputStream();
            FileInfo fileInfo = TransferProtocol.readFileInfo(socketStream);
            Path transferredPath = Paths.get(fileInfo.getName());
            String fileName = transferredPath.getFileName().toString();
            newFile = Paths.get(uploadPath, fileName);
            newFileStream = createFile(newFile);
            fileSizeBytes = fileInfo.getSize();
            speedMeasurer.scheduleAtFixedRate(this::measureSpeed, 0, MEASURE_PERIOD_SEC, TimeUnit.SECONDS);

            byte[] checksum = downloadFile(socketStream, newFileStream);

            // if counted checksum equals checksum from file info, send true
            TransferProtocol.writeACK(socket.getOutputStream(), Arrays.equals(checksum, fileInfo.getHash()));
            newFileStream.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            try {
                if (newFileStream != null) {
                    Files.delete(newFile);
                }
                TransferProtocol.writeACK(socket.getOutputStream(), false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        speedMeasurer.shutdown();
    }

    private double millisToSec(long millis) {
        return millis / 1000.0;
    }

    private void measureSpeed() {
        long startTime = System.currentTimeMillis();
        long startBytes = transferredBytes.get();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        long endBytes = transferredBytes.get();
        double curSpeed = (endBytes - startBytes) / (millisToSec(endTime - startTime) * 1024.0);
        double averSpeed = (fileSizeBytes - transferredBytes.get()) / (((double) transferTimeMillis.get() / 1000.0) * 1024.0);
        System.out.println(socket + ": current speed = " +
                String.format("%,.2f", curSpeed) + "kb/s, " +
                "average speed = " +
                String.format("%,.2f", averSpeed) + "kb/s, " +
                (transferredBytes.get() * 100) / fileSizeBytes + "% was transferred");
        System.out.println();
    }

    //  if the file exists -- changes name and tries to create it with another name
    private OutputStream createFile(Path pathToFile) throws IOException {
        int fileNumber = 0;
        while (true) {
            try {
                return Files.newOutputStream(pathToFile, CREATE_NEW);
            } catch (FileAlreadyExistsException e) {
                pathToFile = Paths.get(uploadPath, "(" + ++fileNumber + ") " + pathToFile.getFileName());
            }
        }
    }

    private byte[] downloadFile(InputStream input, OutputStream out) throws IOException, NoSuchAlgorithmException {
        byte[] bytes = new byte[BUFF_SIZE];
        System.out.println("filesize=" + fileSizeBytes);
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream checksumStream = new DigestInputStream(input, md);
        int readBytes;
        for (long i = 0; i < fileSizeBytes; i += readBytes) {
            readBytes = checksumStream.read(bytes);
            long startMeasuring = System.currentTimeMillis();
            out.write(bytes, 0, readBytes);
            transferredBytes.addAndGet(readBytes);
            long endMeasuring = System.currentTimeMillis();
            transferTimeMillis.addAndGet(endMeasuring - startMeasuring);
        }
        return md.digest();
    }
}
