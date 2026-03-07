package com.barco.auth.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import javax.validation.constraints.NotBlank;

/**
 * Request payload for resetting a user's password.
 * Contains the reset token, the new password, and optionally the IP address of the request.
 * @author Nabeel Ahmed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordResetRequest {

    @NotBlank(message = "token must not be blank")
    private String token;

    @NotBlank(message = "newPassword must not be blank")
    private String newPassword;

    private String ipAddress;

    public PasswordResetRequest() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}

