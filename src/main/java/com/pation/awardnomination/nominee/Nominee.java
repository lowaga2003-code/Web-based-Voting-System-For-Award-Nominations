package com.pation.awardnomination.nominee;

import jakarta.persistence.*; // ඔයාගේ Spring Boot version එක අනුව මේක javax.persistence.* වෙන්නත් පුළුවන්

@Entity
@Table(name = "nominees")
// ENCAPSULATION: Variables are private, keeping data safe. Accessed only via public getters/setters.
public class Nominee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String fullName;
    private String biography;
    private String companyName;
    private String evidenceFileName; // මේ අලුත් variable එක පරණ එකේ තිබ්බෙ නෑ

    private String status = "Draft";

    // Default Constructor: Spring Boot/JPA වලට object එකක් හදාගන්න මේක අනිවාර්යයි (Error 1 එක හැදෙන්නේ මේකෙන්)
    public Nominee() {}

    // Public Getters and Setters (Error 2 එක හැදෙන්නේ setEvidenceFileName එකෙන්)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEvidenceFileName() { return evidenceFileName; }
    public void setEvidenceFileName(String evidenceFileName) { this.evidenceFileName = evidenceFileName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}