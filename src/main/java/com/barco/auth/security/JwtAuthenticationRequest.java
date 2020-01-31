package com.barco.auth.security;

import com.google.gson.Gson;

public class JwtAuthenticationRequest {

    private String username;
    private String password;

    public JwtAuthenticationRequest() { }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}