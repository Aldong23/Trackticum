package com.example.trackticum.models;

public class StudPreRequirement {
    private final String preReqId;
    private final String preReTitle;
    private final String preReqFileId;

    public StudPreRequirement(String preReqId, String preReTitle, String preReqFileId) {
        this.preReqId = preReqId;
        this.preReTitle = preReTitle;
        this.preReqFileId = preReqFileId;
    }

    public String getPreReqId() {
        return preReqId;
    }

    public String getPreReTitle() {
        return preReTitle;
    }

    public String getPreReqFileId() {
        return preReqFileId;
    }
}
