package com.barco.auth.service;

import com.barco.auth.payload.request.LoginRequest;
import com.barco.auth.payload.request.SignupRequest;
import com.barco.auth.payload.request.ForgotPasswordRequest;
import com.barco.auth.payload.request.PasswordResetRequest;
import com.barco.auth.payload.request.TokenRefreshRequest;
import com.barco.auth.payload.request.VerifyEmailRequest;
import com.barco.auth.payload.request.ResendVerificationRequest;
import com.barco.common.payload.APIResponse;

/**
 * @author Nabeel Ahmed
 */
public interface EtlAuthService {

    APIResponse<?> signIn(LoginRequest payload);

    APIResponse<?> signup(SignupRequest payload) throws Exception;

    APIResponse<?> forgotPassword(ForgotPasswordRequest payload) throws Exception;

    APIResponse<?> resetPassword(PasswordResetRequest payload) throws Exception;

    APIResponse<?> refreshToken(TokenRefreshRequest payload) throws Exception;

    APIResponse<?> verifyEmail(VerifyEmailRequest payload) throws Exception;

    APIResponse<?> resendVerification(ResendVerificationRequest payload) throws Exception;

}
