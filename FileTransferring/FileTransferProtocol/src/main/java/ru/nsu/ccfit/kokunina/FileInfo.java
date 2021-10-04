package ru.nsu.ccfit.kokunina;

public class FileInfo {
    public final static int HASH_SIZE = 16;

    public FileInfo(String name, long size, byte[] hash) {
        this.name = name;
        this.size = size;
        this.hash = hash;
    }

    // max 4096 on file name
    private String name;
    // size in bytes
    private long size;

    private byte[] hash;

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public byte[] getHash() {
        return hash;
    }
}
