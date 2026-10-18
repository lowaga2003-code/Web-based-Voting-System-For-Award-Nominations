package com.pation.awardnomination.admin;

import java.util.List;

public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private boolean active;
    private boolean hasVoted;

    public UserInfoResponse(Long id, String username, String email, List<String> roles, boolean active, boolean hasVoted) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.active = active;
        this.hasVoted = hasVoted;
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }
    public boolean isActive() { return active; }
    public boolean isHasVoted() { return hasVoted; }
}
