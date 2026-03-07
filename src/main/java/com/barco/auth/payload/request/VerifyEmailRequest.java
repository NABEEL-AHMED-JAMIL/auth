package com.barco.auth.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import javax.validation.constraints.NotBlank;

/**
 * Request payload for verifying an email address using a token.
 * Contains the verification token and optionally the IP address of the request origin.
 * Validates that the token is not blank.
 * @author Nabeel Ahmed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyEmailRequest {

    @NotBlank(message = "token must not be blank")
    private String token;

    private String ipAddress;

    public VerifyEmailRequest() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

