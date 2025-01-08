package com.example.trackticum.models;

public class ComInterns {

    private final String studID;
    private final String name;
    private final String department;
    private final String image;

    public ComInterns(String studID, String name, String department, String image) {
        this.studID = studID;
        this.name = name;
        this.department = department;
        this.image = image;
    }

    public String getStudID() {
        return studID;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getImage() {
        return image;
    }
}
