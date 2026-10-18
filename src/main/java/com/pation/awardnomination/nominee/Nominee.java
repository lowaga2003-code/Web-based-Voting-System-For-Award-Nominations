package com.pation.awardnomination.nominee;

import jakarta.persistence.*;

@Entity
@Table(name = "nominees")
public class Nominee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fullName;
    private String companyName;

    @Column(columnDefinition = "TEXT") //biography ekata loku text idak hadala denawa
    private String biography;

    private String evidenceFileName;
    private String status = "Draft";

    private String nic; // අලුතින් එකතු කළ NIC field එක

    // Getters and Setters for encapsulations

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getEvidenceFileName() { return evidenceFileName; }
    public void setEvidenceFileName(String evidenceFileName) { this.evidenceFileName = evidenceFileName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
}