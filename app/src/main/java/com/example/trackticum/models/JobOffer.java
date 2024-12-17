package com.example.trackticum.models;

public class JobOffer {
    private final int id;
    private final String jobTitle;

    public JobOffer(int id, String jobTitle) {
        this.id = id;
        this.jobTitle = jobTitle;
    }

    public int getId() {
        return id;
    }

    public String getJobTitle() {
        return jobTitle;
    }
}
