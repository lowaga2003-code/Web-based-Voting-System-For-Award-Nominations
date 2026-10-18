package com.pation.awardnomination.dispute;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents system-generated voting activity logs.
 */
@Entity
@Table(name = "system_activity_logs")
public class SystemActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String voterUsername;

    @Column(nullable = false, length = 50)
    private String actionType; // e.g., VOTE_SUBMITTED, BALLOT_OPENED, IP_COLLISION_ALERT

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 200)
    private String deviceSignature;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(nullable = false, length = 30)
    private String anomalyStatus; // NORMAL, FLAGGED_ANOMALY, INVESTIGATION_CRITICAL

    public SystemActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    public SystemActivityLog(String voterUsername, String actionType, String ipAddress, String deviceSignature, String details, String anomalyStatus) {
        this.voterUsername = voterUsername;
        this.actionType = actionType;
        this.ipAddress = ipAddress;
        this.deviceSignature = deviceSignature;
        this.details = details;
        this.anomalyStatus = anomalyStatus;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVoterUsername() {
        return voterUsername;
    }

    public void setVoterUsername(String voterUsername) {
        this.voterUsername = voterUsername;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceSignature() {
        return deviceSignature;
    }

    public void setDeviceSignature(String deviceSignature) {
        this.deviceSignature = deviceSignature;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getAnomalyStatus() {
        return anomalyStatus;
    }

    public void setAnomalyStatus(String anomalyStatus) {
        this.anomalyStatus = anomalyStatus;
    }
}
