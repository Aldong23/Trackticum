package com.example.trackticum.models;

public class ComCoordinator {
    private final String coordinatorId;
    private final String name;
    private final String department;
    private final String image;

    public ComCoordinator(String coordinatorId, String name, String department, String image) {
        this.coordinatorId = coordinatorId;
        this.name = name;
        this.department = department;
        this.image = image;
    }

    public String getCoordinatorId() {
        return coordinatorId;
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
