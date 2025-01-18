package com.example.trackticum.models;

public class Announcements {
    private final String announcementId;
    private final String title;
    private final String message;
    private final String date;

    public Announcements(String announcementId, String title, String message, String date) {
        this.announcementId = announcementId;
        this.title = title;
        this.message = message;
        this.date = date;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }
}
