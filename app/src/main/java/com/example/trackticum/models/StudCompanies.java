package com.example.trackticum.models;

public class StudCompanies {
    private final String id;
    private final String name;
    private final String logo;
    private final String address;
    private final String description;
    private final String slot;

    public StudCompanies(String id, String name, String logo, String address, String description, String slot) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.address = address;
        this.description = description;
        this.slot = slot;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getSlot() {
        return slot;
    }
}
