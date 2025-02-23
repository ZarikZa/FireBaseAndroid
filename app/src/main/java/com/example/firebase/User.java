package com.example.firebase;

public class User {
    private String id;
    private String role;
    private String email;

    private User(){}
    public User(String id, String role, String email){
        this.role = role;
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
