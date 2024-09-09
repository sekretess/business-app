package com.sekretess.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessions")
public class SessionModel {
    @Id
    private String name;

    @Column(name = "deviceId")
    private int deviceId;

    @Column(name = "sessionRecord",length = 10000)
    private String sessionRecord;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getSessionRecord() {
        return sessionRecord;
    }

    public void setSessionRecord(String sessionRecord) {
        this.sessionRecord = sessionRecord;
    }
}
