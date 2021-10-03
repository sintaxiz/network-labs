package ru.nsu.ccfit.kokunina;


public class FTServerApplication {
    final static private int DEFAULT_PORT = 9999;

    public static void main(String[] args) {
        int PORT = args.length < 2 ? DEFAULT_PORT : Integer.parseInt(args[1]);
        FileTransferringService tfService = new FileTransferringService(PORT);
        tfService.run();
    }
}
