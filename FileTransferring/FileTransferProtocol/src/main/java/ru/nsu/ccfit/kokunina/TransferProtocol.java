package ru.nsu.ccfit.kokunina;

import java.io.*;

public class TransferProtocol {
    public static FileInfo readFileInfo(InputStream in) throws IOException {
        DataInputStream inputStream = new DataInputStream(in);
        String fileName = inputStream.readUTF();
        long fileSize = inputStream.readLong();
        return new FileInfo(fileName, fileSize);
    }

    public static void writeFileInfo(OutputStream out, FileInfo fileInfo) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(out);
        outputStream.writeUTF(fileInfo.getName());
        outputStream.writeLong(fileInfo.getSize());
    }

    public static boolean readACK(InputStream in) throws IOException {
        return new DataInputStream(in).readBoolean();
    }

    public static void writeACK(OutputStream out, boolean ack) throws IOException {
        new DataOutputStream(out).writeBoolean(ack);
    }

}
