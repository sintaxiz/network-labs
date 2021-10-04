package ru.nsu.ccfit.kokunina;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferringService implements Runnable {
    private final String UPLOADS_PATH = "./uploads";

    private final int port;
    public FileTransferringService(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Files.createDirectory(Paths.get(UPLOADS_PATH));
        } catch (FileAlreadyExistsException e) {
            // it's okay, continue
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ExecutorService connectionsPool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                System.out.println("Accepted new connection: " + socket);
                connectionsPool.execute(new ClientConnection(socket, UPLOADS_PATH));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionsPool.shutdown();
    }
}
