package com.pation.awardnomination.nominee;

import jakarta.persistence.*;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String interviewDate; // උදා: 2026-10-15
    private String interviewTime; // උදා: 10:00 AM
    private String location;      // උදා: Hall A / Zoom Link

    private String status = "Available"; // Available හෝ Booked

    private String nomineeNic;     // Slot එක වෙන් කරගත් Nominee ගේ NIC එක

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getInterviewDate() { return interviewDate; }
    public void setInterviewDate(String interviewDate) { this.interviewDate = interviewDate; }

    public String getInterviewTime() { return interviewTime; }
    public void setInterviewTime(String interviewTime) { this.interviewTime = interviewTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNomineeNic() { return nomineeNic; }
    public void setNomineeNic(String nomineeNic) { this.nomineeNic = nomineeNic; }
}
