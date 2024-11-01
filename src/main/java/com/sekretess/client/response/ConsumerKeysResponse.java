package com.sekretess.client.response;


public class ConsumerKeysResponse {
    private String username;
    private String ik;
    private String opk;
    private int regId;
    private String spkSignature;
    private String spk;
    private String spkId;
    private String pqspk;
    private String pqspkID;
    private String pqSpkSignature;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIk() {
        return ik;
    }

    public void setIk(String ik) {
        this.ik = ik;
    }

    public String getOpk() {
        return opk;
    }

    public void setOpk(String opk) {
        this.opk = opk;
    }

    public int getRegId() {
        return regId;
    }

    public void setRegId(int regId) {
        this.regId = regId;
    }

    public String getSpkSignature() {
        return spkSignature;
    }

    public void setSpkSignature(String spkSignature) {
        this.spkSignature = spkSignature;
    }

    public String getSpk() {
        return spk;
    }

    public void setSpk(String spk) {
        this.spk = spk;
    }

    public String getSpkId() {
        return spkId;
    }

    public void setSpkId(String spkId) {
        this.spkId = spkId;
    }

    public String getPqspk() {
        return pqspk;
    }

    public void setPqspk(String pqspk) {
        this.pqspk = pqspk;
    }

    public String getPqspkID() {
        return pqspkID;
    }

    public void setPqspkID(String pqspkID) {
        this.pqspkID = pqspkID;
    }

    public String getPqSpkSignature() {
        return pqSpkSignature;
    }

    public void setPqSpkSignature(String pqSpkSignature) {
        this.pqSpkSignature = pqSpkSignature;
    }

    @Override
    public String toString() {
        return "ConsumerKeysResponse{" +
                "username='" + username + '\'' +
                ", ik='" + ik + '\'' +
                ", opk='" + opk + '\'' +
                ", regId=" + regId +
                ", spkSignature='" + spkSignature + '\'' +
                ", spk='" + spk + '\'' +
                ", spkId='" + spkId + '\'' +
                ", pqspk='" + pqspk + '\'' +
                ", pqspkID='" + pqspkID + '\'' +
                ", pqSpkSignature='" + pqSpkSignature + '\'' +
                '}';
    }
}
