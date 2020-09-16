package com.barco.auth.api;

import com.barco.auth.service.AppUserService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.UserDTO;
import com.barco.model.enums.ApiCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/app.json", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = { "Barco-AppUser Admin := Barco-AppUser EndPoint" })
public class AppAdminUserRestApi {

    public Logger logger = LogManager.getLogger(AppAdminUserRestApi.class);

    @Autowired
    private AppUserService appUserService;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/registrationByAdmin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "User Registration By Admin.", notes = "You have to provide user Information to save in Barco DB.")
    public @ResponseBody
    ResponseDTO registrationByAdmin(@RequestBody UserDTO userDTO) {
        ResponseDTO response = null;
        try {
            logger.info("Request for registrationByAdmin " + userDTO);
            response = this.appUserService.saveUserRegistrationByAdmin(userDTO);
        } catch (Exception ex) {
            logger.info("Error during registrationByAdmin " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500,
                    ApplicationConstants.UNEXPECTED_ERROR + ex.getMessage(), userDTO);
        }
        return response;
    }

}
