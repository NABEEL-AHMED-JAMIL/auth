package com.barco.auth.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Request payload for initiating the forgot password process.
 * Contains the email of the user requesting a password reset and optionally the IP address of the request.
 * @author Nabeel Ahmed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgotPasswordRequest {

    @Email(message = "email must be a valid email")
    @NotBlank(message = "email must not be blank")
    private String email;

    private String ipAddress;

    public ForgotPasswordRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

