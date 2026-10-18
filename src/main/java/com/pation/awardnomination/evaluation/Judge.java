package com.pation.awardnomination.evaluation;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** [INTEGRATION] table name set to "judges" (same style as nominees, interviews ...). */
@Entity
@Table(name = "judges")
public class Judge extends BaseEntity {   // INHERITANCE

    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;
    @NotBlank(message = "Organization is required")
    private String organization;
    private String expertise;

    // ENCAPSULATION: private fields + public getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }
}
