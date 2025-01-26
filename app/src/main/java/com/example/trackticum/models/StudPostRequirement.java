package com.example.trackticum.models;

public class StudPostRequirement {
    private final String postReqId;
    private final String postReqTitle;
    private final String postReqFileId;

    public StudPostRequirement(String postReqId, String postReqTitle, String postReqFileId) {
        this.postReqId = postReqId;
        this.postReqTitle = postReqTitle;
        this.postReqFileId = postReqFileId;
    }

    public String getPostReqId() {
        return postReqId;
    }

    public String getPostReqTitle() {
        return postReqTitle;
    }

    public String getPostReqFileId() {
        return postReqFileId;
    }
}
