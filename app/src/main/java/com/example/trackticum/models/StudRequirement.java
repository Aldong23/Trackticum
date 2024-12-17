package com.example.trackticum.models;

public class StudRequirement {
    private final String documentRequirementId;
    private final String documentBeforeOjtId;
    private final String documentRequirementTitle;
    private final String documentBeforeOjtFile;

    public StudRequirement(String documentRequirementId, String documentBeforeOjtId, String documentRequirementTitle, String documentBeforeOjtFile) {
        this.documentRequirementId = documentRequirementId;
        this.documentBeforeOjtId = documentBeforeOjtId;
        this.documentRequirementTitle = documentRequirementTitle;
        this.documentBeforeOjtFile = documentBeforeOjtFile;
    }

    public String getDocumentRequirementId() {
        return documentRequirementId;
    }

    public String getDocumentBeforeOjtId() {
        return documentBeforeOjtId;
    }

    public String getDocumentRequirementTitle() {
        return documentRequirementTitle;
    }

    public String getDocumentBeforeOjtFile() {
        return documentBeforeOjtFile;
    }
}
