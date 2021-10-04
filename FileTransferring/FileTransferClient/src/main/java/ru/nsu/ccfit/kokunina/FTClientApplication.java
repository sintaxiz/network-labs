package ru.nsu.ccfit.kokunina;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FTClientApplication {
    private final static int BUFF_SIZE = 4096;

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket();
        if (args.length < 3) {
            System.out.println("usage: file_name server_address server_port");
            return;
        }
        String filePath = args[0];
        String serverName = args[1];
        int serverPort = Integer.parseInt(args[2]);


        try {
            Path file = Paths.get(filePath);
            socket.connect(new InetSocketAddress(serverName, serverPort));
            OutputStream outputStream = socket.getOutputStream();

            FileInfo fileInfo = new FileInfo(filePath, Files.size(file));
            TransferProtocol.writeFileInfo(outputStream, fileInfo);
            uploadFile(outputStream, Files.newInputStream(file));

            boolean isSuccessTransfer = TransferProtocol.readACK(socket.getInputStream()); // wait while all file was transmitted
            if (isSuccessTransfer) {
                System.out.println("horay! successfully transmitted file " + file + " to server c:");
            } else {
                System.out.println("oh no! there is some error on server side while transferring file :c");
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void uploadFile(OutputStream outputStream, InputStream fileStream) throws IOException {
        byte[] buff = new byte[BUFF_SIZE];
        int readBytes;
        while ((readBytes = fileStream.read(buff)) > 0) {
            outputStream.write(buff, 0, readBytes);
        }
    }
}
