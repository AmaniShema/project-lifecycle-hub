package com.amani.projecthub.dto;

public class AuthResponse {

    private String token;
    private String name;
    private String email;
    private String type = "Bearer";

    public AuthResponse() {}

    public AuthResponse(String token, String name, String email) {
        this.token = token;
        this.name = name;
        this.email = email;
    }

    public String getToken() { return token; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getType() { return type; }

    public void setToken(String token) { this.token = token; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setType(String type) { this.type = type; }
}