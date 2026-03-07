package com.barco.auth.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import javax.validation.constraints.NotBlank;

/**
 * Request payload for refreshing an authentication token.
 * Contains the refresh token and optionally the IP address of the request origin.
 * Validates that the refresh token is not blank.
 * @author Nabeel Ahmed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenRefreshRequest {

    @NotBlank(message = "refreshToken must not be blank")
    private String refreshToken;

    private String ipAddress;

    public TokenRefreshRequest() {}

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}

