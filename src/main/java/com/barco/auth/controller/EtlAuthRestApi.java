package com.barco.auth.controller;

import com.barco.auth.payload.request.LoginRequest;
import com.barco.auth.payload.request.SignupRequest;
import com.barco.auth.payload.request.ForgotPasswordRequest;
import com.barco.auth.payload.request.PasswordResetRequest;
import com.barco.auth.payload.request.TokenRefreshRequest;
import com.barco.auth.payload.request.VerifyEmailRequest;
import com.barco.auth.payload.request.ResendVerificationRequest;
import com.barco.auth.service.EtlAuthService;
import com.barco.common.payload.APIResponse;
import com.barco.common.utility.BarcoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * Api use to perform crud operation
 * @author Nabeel Ahmed
 */
@RestController
@RequestMapping(value = "/auth")
public class EtlAuthRestApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtlAuthRestApi.class);

    private final EtlAuthService authService;

    public EtlAuthRestApi(EtlAuthService authService) {
        this.authService = authService;
    }

    /**
     * Method to handle user sign-in requests.
     * It accepts user credentials and returns an authentication token if the credentials are valid.
     * @param payload The request payload containing user credentials (e.g., username and password).
     * @return ResponseEntity containing the authentication token
     * if sign-in is successful, or an error message if sign-in fails.
     * */
    @RequestMapping(value="/signIn", method=RequestMethod.POST)
    public ResponseEntity<?> signIn(@Valid @RequestBody LoginRequest payload) {
        LOGGER.debug("signIn request payload: {}", payload);
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.authService.signIn(payload);
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.badRequest("Invalid username or password.");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while signing in", ex);
            apiResponse = APIResponse.error("Unexpected error while signing in");
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }

    /**
     * Sign-up API for new users.
     * It accepts user registration details and creates a new user account
     * if the provided information is valid.
     * @return ResponseEntity indicating the success or
     * failure of the sign-up process, along with any relevant messages or data.
     * */
    @RequestMapping(value="/signUp", method=RequestMethod.POST)
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest payload) {
        LOGGER.debug("signup request payload: {}", payload);
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.authService.signup(payload);
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.badRequest("Signup failed. Please check the provided information and try again.");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while signing up", ex);
            apiResponse = APIResponse.error("Unexpected error while signing up");
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }

    /**
     * Forgot password API for users who have forgotten their password.
     * It accepts the user's email address and sends a password reset link or
     * instructions to the provided email if it is associated with an account.
     * @return ResponseEntity indicating the success or failure of the
     * forgot password process, along with any relevant messages or data.
     * */
    @RequestMapping(value="/forgot-password", method=RequestMethod.POST)
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest payload) {
        LOGGER.debug("forgotPassword request payload: {}", payload);
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.authService.forgotPassword(payload);
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.badRequest("Forgot password request could not be processed. Please verify the email and try again.");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while processing forgotPassword", ex);
            apiResponse = APIResponse.error("Unexpected error while processing forgotPassword");
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }

    /**
     * Reset password API for users who want to reset their password.
     * It accepts the user's email address and new password, and
     * updates the user's password if the provided information is valid.
     * @return ResponseEntity indicating the success or failure of the reset password process,
     * along with any relevant messages or data.
     * */
    @RequestMapping(value="/reset-password", method=RequestMethod.POST)
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest payload) {
        LOGGER.debug("resetPassword request payload: {}", payload);
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.authService.resetPassword(payload);
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.badRequest("Reset password request failed. Please ensure the reset token and passwords are correct.");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while processing resetPassword", ex);
            apiResponse = APIResponse.error("Unexpected error while processing resetPassword");
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }

    /**
     * Refresh token API for users who want to refresh their authentication token.
     * It accepts a valid refresh token and returns a new authentication token if the refresh token is valid.
     * @return ResponseEntity containing the new authentication token if the refresh is successful,
     * or an error message if the refresh fails.
     * */
    @RequestMapping(value="/refresh-token", method=RequestMethod.POST)
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest payload) {
        LOGGER.debug("refreshToken request payload: {}", payload);
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.authService.refreshToken(payload);
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.badRequest("Refresh token request failed. Please provide a valid refresh token.");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while processing refreshToken", ex);
            apiResponse = APIResponse.error("Unexpected error while processing refreshToken");
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }

    /**
     * Verify email API for users who want to verify their email address.
     * It accepts a verification token and verifies the user's email if the token is valid.
     * @return ResponseEntity indicating the success or failure of the email verification process,
     * along with any relevant messages or data.
     * */
    @RequestMapping(value="/verify-email", method=RequestMethod.POST)
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest payload) {
        LOGGER.debug("verifyEmail request payload: {}", payload);
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.authService.verifyEmail(payload);
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.badRequest("Email verification failed. Please check the verification token and try again.");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while processing verifyEmail", ex);
            apiResponse = APIResponse.error("Unexpected error while processing verifyEmail");
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }

    /**
     * Resend verification email API for users who want to resend the email verification link.
     * It accepts the user's email address and resends the verification email if the email is associated with an account.
     * @return ResponseEntity indicating the success or failure of the resend verification email process,
     * along with any relevant messages or data.
     * */
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest payload) {
        LOGGER.debug("resendVerification request payload: {}", payload);
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.authService.resendVerification(payload);
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.badRequest("Resend verification request failed. Please verify the email and try again.");
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while processing resendVerification", ex);
            apiResponse = APIResponse.error("Unexpected error while processing resendVerification");
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }

}
