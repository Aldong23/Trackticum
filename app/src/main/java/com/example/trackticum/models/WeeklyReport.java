package com.example.trackticum.models;

public class WeeklyReport {
    private final String weeklyID;
    private final String studID;
    private final String title;
    private final String evaluation;
    private final String supervisorComment;
    private final String isSigned;
    private final String date;

    public WeeklyReport(String weeklyID, String studID, String title, String evaluation, String supervisorComment, String isSigned, String date) {
        this.weeklyID = weeklyID;
        this.studID = studID;
        this.title = title;
        this.evaluation = evaluation;
        this.supervisorComment = supervisorComment;
        this.isSigned = isSigned;
        this.date = date;
    }

    public String getWeeklyID() {
        return weeklyID;
    }

    public String getStudID() {
        return studID;
    }

    public String getTitle() {
        return title;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public String getSupervisorComment() {
        return supervisorComment;
    }

    public String getIsSigned() {
        return isSigned;
    }

    public String getDate() {
        return date;
    }
}
