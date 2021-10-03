package ru.nsu.ccfit.kokunina;

public class FileInfo {

    public FileInfo(String name, long size) {
        this.name = name;
        this.size = size;
    }

    // max 4096 on file name
    private String name;
    // size in bytes
    private long size;

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
