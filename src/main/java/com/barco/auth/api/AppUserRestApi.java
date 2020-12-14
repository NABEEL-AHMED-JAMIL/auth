package com.barco.auth.api;

import com.barco.auth.service.AppUserService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.UserDTO;
import com.barco.model.enums.ApiCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/public.json", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = { "Barco-AppUser := Barco-AppUser EndPoint" })
public class AppUserRestApi {

    public Logger logger = LogManager.getLogger(AppUserRestApi.class);

    @Autowired
    private AppUserService appUserService;

    // registration q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/registration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "User Registration Rest Api.", notes = "Endpoint help to register the user.")
    public ResponseDTO registration(@RequestBody UserDTO userDTO) {
        ResponseDTO response = null;
        try {
            logger.info("Request for registration " + userDTO);
            response = this.appUserService.saveUserRegistration(userDTO);
        } catch (Exception ex) {
            logger.info("Error during registration " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

    // signupSuccess q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/signupSuccess", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Signup Success REST API.", notes = "Endpoint help to signup success process.")
    public ResponseDTO signupSuccess(@RequestParam String token) {
        ResponseDTO response = null;
        try {
            logger.info("Request for signupSuccess " + token);
            if (token == null || !(token.length() > 0)) {
                return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.REQUEST_CANNOT_BE_PROCESSED);
            }
            response = this.appUserService.emailTokenVerification(token);
        } catch (Exception ex) {
            logger.info("Error during signupSuccess " + ExceptionUtil.getRootCause(ex));
            return new ResponseDTO(ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

    // forgetPassword q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/forgetPassword", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Forget Password REST API.", notes = "Endpoint help for forget password it will reset through your email.")
    public ResponseDTO forgetPassword(@RequestParam String email) {
        ResponseDTO response = null;
        try {
            logger.info("Request for forgetPassword " + email);
            if (email == null || !(email.length() > 0)) {
                return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.INVALID_REQUEST, email);
            }
            response = this.appUserService.forgetPassword(email);
        } catch (Exception ex) {
            logger.info("Error during forgetPassword " + ExceptionUtil.getRootCause(ex));
            return new ResponseDTO(ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

    // resetPassword q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reset Password REST API.", notes = "Endpoint help for Reset Password.")
    public ResponseDTO resetPassword(@RequestBody UserDTO userDTO) {
        ResponseDTO response = null;
        try {
            logger.info("Request for resetPassword " + userDTO);
            if (userDTO.getToken() == null || !(userDTO.getToken().length() > 0) ||
                    userDTO.getToken() == null || !(userDTO.getToken().length() > 0)) {
                return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.INVALID_REQUEST);
            }
            if(StringUtils.isEmpty(userDTO.getPassword())) {
                return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.PASSWORD_SHOULD_NOT_BE_EMPTY);
            }
            response = this.appUserService.resetPassword(userDTO);
        } catch (Exception ex) {
            logger.info("Error during resetPassword " + ExceptionUtil.getRootCause(ex));
            return new ResponseDTO(ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

}
