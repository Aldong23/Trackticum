package com.example.trackticum.models;

public class StudSkill {
    private final int id;
    private final String skillTitle;

    public StudSkill(int id, String skillTitle) {
        this.id = id;
        this.skillTitle = skillTitle;
    }

    public int getId() {
        return id;
    }

    public String getSkillTitle() {
        return skillTitle;
    }
}
