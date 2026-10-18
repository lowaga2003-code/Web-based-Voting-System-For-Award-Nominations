package com.pation.awardnomination.dispute;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents alerts/notifications sent to the user.
 */
@Entity
@Table(name = "dispute_notifications")
public class DisputeNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long ticketId;

    @Column(nullable = false, length = 30)
    private String ticketCode;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 40)
    private String type; // EVIDENCE_REQUESTED, RESOLVED

    @Column(name = "is_read", nullable = false) // [INTEGRATION] READ is a reserved word in SQL Server (worked in H2 only)
    private boolean read;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public DisputeNotification() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public DisputeNotification(Long userId, Long ticketId, String ticketCode, String title, String message, String type) {
        this.userId = userId;
        this.ticketId = ticketId;
        this.ticketCode = ticketCode;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
