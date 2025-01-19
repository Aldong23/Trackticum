package com.example.trackticum.models;

public class Notification {
    private final String notificationId;
    private final String senderName;
    private final String message;
    private final String type;
    private final String date;
    private final String isRead;

    public Notification(String notificationId, String senderName, String message, String type, String date, String isRead) {
        this.notificationId = notificationId;
        this.senderName = senderName;
        this.message = message;
        this.type = type;
        this.date = date;
        this.isRead = isRead;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getIsRead() {
        return isRead;
    }
}
