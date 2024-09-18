package com.barco.auth.service;

import com.barco.model.dto.request.*;
import com.barco.model.dto.response.AppResponse;

/**
 * Api use to perform crud operation
 * @author Nabeel Ahmed
 */
public interface AuthService extends RootService {

    public AppResponse signInAppUser(LoginRequest payload) throws Exception;

    public AppResponse signupAppUser(SignupRequest payload) throws Exception;

    public AppResponse forgotPassword(ForgotPasswordRequest payload) throws Exception;

    public AppResponse resetPassword(PasswordResetRequest payload) throws Exception;

    public AppResponse authClamByRefreshToken(TokenRefreshRequest payload)  throws Exception;

    public AppResponse logoutAppUser(TokenRefreshRequest payload)  throws Exception;

}
