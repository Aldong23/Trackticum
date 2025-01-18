package com.example.trackticum.models;

public class Activities {
    private final String activityId;
    private final String weeklyId;
    private final String date;
    private final String activity;

    public Activities(String activityId, String weeklyId, String date, String activity) {
        this.activityId = activityId;
        this.weeklyId = weeklyId;
        this.date = date;
        this.activity = activity;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getWeeklyId() {
        return weeklyId;
    }

    public String getDate() {
        return date;
    }

    public String getActivity() {
        return activity;
    }
}
