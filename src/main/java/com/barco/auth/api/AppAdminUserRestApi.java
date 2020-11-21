package com.barco.auth.api;

import com.barco.auth.service.AccessServiceService;
import com.barco.auth.service.AppUserService;
import com.barco.auth.service.AuthorityService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.AccessServiceDto;
import com.barco.model.dto.AuthorityDto;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/app.json", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = { "Barco-AppUser Admin := Barco-AppUser EndPoint" })
public class AppAdminUserRestApi {

    public Logger logger = LogManager.getLogger(AppAdminUserRestApi.class);

    @Autowired
    private AppUserService appUserService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private AccessServiceService accessServiceService;


    // createAuthority q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @RequestMapping(value = "/createAuthority", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create Authority.", notes = "This method use to create the role for user.")
    public ResponseDTO createAuthority(@RequestBody AuthorityDto authority) {
        ResponseDTO response = null;
        try {
            logger.info("Request for createAuthority " + authority);
            // method use to create new authority
            response = this.authorityService.createAuthority(authority);
        } catch (Exception ex) {
            logger.info("Error during createAuthority " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

    // createAccessService q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @RequestMapping(value = "/createAccessService", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create Access Service.", notes = "This method use to create the access service for user.")
    public ResponseDTO createAccessService(@RequestBody AccessServiceDto accessService) {
        ResponseDTO response = null;
        try {
            logger.info("Request for createAccessService " + accessService);
            // method use to create new authority
            response = this.accessServiceService.createAccessService(accessService);
        } catch (Exception ex) {
            logger.info("Error during createAccessService " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/registrationByAdmin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "User Registration By Admin.", notes = "You have to provide user Information to save in Barco DB.")
    public ResponseDTO registrationByAdmin(@RequestBody UserDTO userDTO) {
        ResponseDTO response = null;
        try {
            logger.info("Request for registrationByAdmin " + userDTO);
            response = this.appUserService.saveUserRegistrationByAdmin(userDTO);
        } catch (Exception ex) {
            logger.info("Error during registrationByAdmin " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

    // create auth process
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @RequestMapping(value = "/fetchSuperAdminUserList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Fetch Super Admin User.", notes = "Fetch Super Admin User For List In Admin 's.")
    public ResponseDTO fetchSuperAdminUserList(@RequestParam(name = "superAdminId") Long superAdminId) {
        ResponseDTO response = null;
        try {
            logger.info("Request for fetchSuperAdminUserList " + superAdminId);
            response = this.appUserService.fetchSuperAdminUserList(superAdminId);
        } catch (Exception ex) {
            logger.info("Error during fetchSuperAdminUserList " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

}
