package com.example.trackticum.models;

public class ComCoordinatorConversation {

    private final String messageID;
    private final String userID;
    private final String name;
    private final String lastMessage;
    private final String image;
    private final String seen;

    public ComCoordinatorConversation(String messageID, String userID, String name, String lastMessage, String image, String seen) {
        this.messageID = messageID;
        this.userID = userID;
        this.name = name;
        this.lastMessage = lastMessage;
        this.image = image;
        this.seen = seen;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getImage() {
        return image;
    }

    public String getSeen() {
        return seen;
    }
}
