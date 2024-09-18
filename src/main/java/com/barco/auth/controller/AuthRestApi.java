package com.barco.auth.controller;

import com.barco.auth.service.AuthService;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.request.*;
import com.barco.model.dto.response.AppResponse;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * Api use to perform crud operation
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins="*")
@RequestMapping(value = "/auth.json")
@Api(value = "Auth Rest Api",
   description = "Auth Service : Use to perform the authentication and authorization. ")
public class AuthRestApi {

    private Logger logger = LoggerFactory.getLogger(AuthRestApi.class);

    @Autowired
    private AuthService authService;

    /**
     * @apiName :- signInAppUser
     * @apiNote :- Api use to sign In the appUser
     * @param httpServletRequest
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to signIn the app user.", response = ResponseEntity.class)
    @RequestMapping(value="/signInAppUser", method=RequestMethod.POST)
    public ResponseEntity<?> signInAppUser(HttpServletRequest httpServletRequest) {
        try {
            String requestData = httpServletRequest.getReader().lines().collect(Collectors.joining());
            LoginRequest requestPayload = new Gson().fromJson(requestData, LoginRequest.class);
            requestPayload.setIpAddress(BarcoUtil.getRequestIP(httpServletRequest));
            return new ResponseEntity<>(this.authService.signInAppUser(requestPayload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while signInAppUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- signupAppUser
     * @apiNote :- Api use support to forgot password
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to signUp the app user.", response = ResponseEntity.class)
    @RequestMapping(value="/signupAppUser", method=RequestMethod.POST)
    public ResponseEntity<?> signupAppUser(HttpServletRequest httpServletRequest) {
        try {
            String requestData = httpServletRequest.getReader().lines().collect(Collectors.joining());
            SignupRequest payload = new Gson().fromJson(requestData, SignupRequest.class);
            payload.setIpAddress(BarcoUtil.getRequestIP(httpServletRequest));
            return new ResponseEntity<>(this.authService.signupAppUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while signupAppUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- forgotPassword
     * @apiNote :- Api use support to forgot password
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to send forgot password email.", response = ResponseEntity.class)
    @RequestMapping(value="/forgotPassword", method=RequestMethod.POST)
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest payload) {
        try {
            return new ResponseEntity<>(this.authService.forgotPassword(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while forgotPassword ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- resetPassword
     * @apiNote :- Api use support to forgot password
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to send reset password email.", response = ResponseEntity.class)
    @RequestMapping(value="/resetPassword", method=RequestMethod.POST)
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest payload) {
        try {
            return new ResponseEntity<>(this.authService.resetPassword(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while resetPassword ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- authClamByRefreshToken
     * @apiNote :- Api use to get refreshToken for appUser
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to clam the new token base on refresh token password email.", response = ResponseEntity.class)
    @RequestMapping(value="/authClamByRefreshToken", method=RequestMethod.POST)
    public ResponseEntity<?> authClamByRefreshToken(@RequestBody TokenRefreshRequest payload) {
        try {
            return new ResponseEntity<>(this.authService.authClamByRefreshToken(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while authClamByRefreshToken ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- logoutAppUser
     * @apiNote :- Api use to delete refreshToken for appUser
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to logout the app user.", response = ResponseEntity.class)
    @RequestMapping(value="/logoutAppUser", method=RequestMethod.POST)
    public ResponseEntity<?> logoutAppUser(@RequestBody TokenRefreshRequest payload) {
        try {
            return new ResponseEntity<>(this.authService.logoutAppUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while logoutAppUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
