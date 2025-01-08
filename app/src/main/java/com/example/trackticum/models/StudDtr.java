package com.example.trackticum.models;

public class StudDtr {
    private final String dtrId;
    private final String studID;
    private final String date;
    private final String amTimeIn;
    private final String amTimeOut;
    private final String pmTimeIn;
    private final String pmTimeOut;
    private final String isSigned;

    public StudDtr(String dtrId, String studID, String date, String amTimeIn, String amTimeOut, String pmTimeIn, String pmTimeOut, String isSigned) {
        this.dtrId = dtrId;
        this.studID = studID;
        this.date = date;
        this.amTimeIn = amTimeIn;
        this.amTimeOut = amTimeOut;
        this.pmTimeIn = pmTimeIn;
        this.pmTimeOut = pmTimeOut;
        this.isSigned = isSigned;
    }

    public String getDtrId() {
        return dtrId;
    }

    public String getStudID() {
        return studID;
    }

    public String getDate() {
        return date;
    }

    public String getAmTimeIn() {
        return amTimeIn;
    }

    public String getAmTimeOut() {
        return amTimeOut;
    }

    public String getPmTimeIn() {
        return pmTimeIn;
    }

    public String getPmTimeOut() {
        return pmTimeOut;
    }

    public String getIsSigned() {
        return isSigned;
    }
}
