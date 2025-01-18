package com.example.trackticum.models;

public class AnnAttachment {
    private final String id;
    private final String file;

    public AnnAttachment(String id, String file) {
        this.id = id;
        this.file = file;
    }

    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }
}
