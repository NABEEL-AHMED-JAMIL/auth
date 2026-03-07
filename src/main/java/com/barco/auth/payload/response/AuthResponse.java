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
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds until expiry
    private Long issuedAt; // epoch millis
    private String userId;
    private String username;
    private String activeProfile;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.issuedAt = System.currentTimeMillis();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public AuthResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public AuthResponse setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public AuthResponse setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public AuthResponse setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    public Long getIssuedAt() {
        return issuedAt;
    }

    public AuthResponse setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public AuthResponse setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AuthResponse setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public AuthResponse setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
