package io.sekretess.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_sessions")
public class GroupSessionModel {
    @Id
    private String name;

    @Column(name = "deviceId")
    private int deviceId;

    @Column(name = "distributionId")
    private String distributionId;

    @Column(name = "sessionRecord",length = 10000)
    private String sessionRecord;

    @Column(name = "distributionMessage",length = 10000)
    private String distributionMessage;

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

    public String getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(String distributionId) {
        this.distributionId = distributionId;
    }

    public String getDistributionMessage() {
        return distributionMessage;
    }

    public void setDistributionMessage(String distributionMessage) {
        this.distributionMessage = distributionMessage;
    }

    public String getSessionRecord() {
        return sessionRecord;
    }

    public void setSessionRecord(String sessionRecord) {
        this.sessionRecord = sessionRecord;
    }
}
