package com.barco.auth.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

/**
 * Authentication response returned after successful sign-in.
 * Contains access and refresh tokens and related metadata.
 * @author Nabeel Ahmed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentAccountResponse {

    private String uuid;
    private String username;
    private String email;
    private String accountProfile;

    public CurrentAccountResponse() {}

    public String getUuid() {
        return uuid;
    }

    public CurrentAccountResponse setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public CurrentAccountResponse setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public CurrentAccountResponse setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getAccountProfile() {
        return accountProfile;
    }

    public CurrentAccountResponse setActiveProfile(String accountProfile) {
        this.accountProfile = accountProfile;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
