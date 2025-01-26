package com.example.trackticum.models;

public class Message {
    private final String image;
    private final String senderId;
    private final String receiverId;
    private final String senderType;
    private final String message;
    private final String createdAt;

    public Message(String image, String senderId, String receiverId, String senderType, String message, String createdAt) {
        this.image = image;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderType = senderType;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getImage() {
        return image;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderType() {
        return senderType;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
