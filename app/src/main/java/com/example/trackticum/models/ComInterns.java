package com.example.trackticum.models;

public class ComInterns {

    private final String studID;
    private final String name;
    private final String department;
    private final String image;
    private final String deployeddate;
    private final String trainingDuration;
    private final String hoursRendered;
    private final String progress;

    public ComInterns(String studID, String name, String department, String image, String deployeddate, String trainingDuration, String hoursRendered, String progress) {
        this.studID = studID;
        this.name = name;
        this.department = department;
        this.image = image;
        this.deployeddate = deployeddate;
        this.trainingDuration = trainingDuration;
        this.hoursRendered = hoursRendered;
        this.progress = progress;
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

    public String getDeployeddate() {
        return deployeddate;
    }

    public String getTrainingDuration() {
        return trainingDuration;
    }

    public String getHoursRendered() {
        return hoursRendered;
    }

    public String getProgress() {
        return progress;
    }
}
