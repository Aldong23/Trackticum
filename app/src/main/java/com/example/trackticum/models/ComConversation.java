package com.example.trackticum.models;

public class ComConversation {

    private final String messageID;
    private final String studID;
    private final String name;
    private final String lastMessage;
    private final String image;
    private final String seen;

    public ComConversation(String messageID, String studID, String name, String lastMessage, String image, String seen) {
        this.messageID = messageID;
        this.studID = studID;
        this.name = name;
        this.lastMessage = lastMessage;
        this.image = image;
        this.seen = seen;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getStudID() {
        return studID;
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
