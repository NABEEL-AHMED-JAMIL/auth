package com.barco.auth.controller;

import com.barco.auth.service.AppUserService;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.ExceptionUtil;
import com.barco.common.utility.excel.ExcelUtil;
import com.barco.model.dto.request.AppUserRequest;
import com.barco.model.dto.request.EnVariablesRequest;
import com.barco.model.dto.request.UpdateUserProfileRequest;
import com.barco.model.dto.response.AppResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Api use to perform crud operation
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins="*")
@RequestMapping(value = "/appUser.json")
@Api(value = "App Rest Api",
   description = "AppUser Service : Service related to the user management.")
public class AppUserRestApi extends RootRestApi {

    private Logger logger = LoggerFactory.getLogger(AppUserRestApi.class);

    @Autowired
    private AppUserService appUserService;

    /**
     * @apiName :- fetchAppUserProfile
     * @apiNote :- Api use to fetch app user profile
     * @param username
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch the app user profile.", response = ResponseEntity.class)
    @RequestMapping(value = "/fetchAppUserProfile", method = RequestMethod.GET)
    public ResponseEntity<?> fetchAppUserProfile(@RequestParam String username) {
        try {
            return new ResponseEntity<>(this.appUserService.fetchAppUserProfile(username), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchAppUserProfile ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updateAppUserEnvVariable
     * @apiNote :- Api use to update app user env variable
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to update the app user env variable value.", response = ResponseEntity.class)
    @RequestMapping(value = "/updateAppUserEnvVariable", method = RequestMethod.POST)
    public ResponseEntity<?> updateAppUserEnvVariable(@RequestBody EnVariablesRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.appUserService.updateAppUserEnvVariable(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updateAppUserEnvVariable ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updateAppUserPassword
     * @apiNote :- Api use to update app user password
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to update the app user password.", response = ResponseEntity.class)
    @RequestMapping(value = "/updateAppUserPassword", method = RequestMethod.POST)
    public ResponseEntity<?> updateAppUserPassword(@RequestBody UpdateUserProfileRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.appUserService.updateAppUserPassword(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updateAppUserPassword ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- addAppUserAccount
     * @apiNote :- Api use to add app user account
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to add new app user account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(value = "/addAppUserAccount", method = RequestMethod.POST)
    public ResponseEntity<?> addAppUserAccount(@RequestBody AppUserRequest payload) {
        try {
            // user session detail
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.appUserService.addAppUserAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while addAppUserAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updateAppUserAccount
     * @apiNote :- Api use to update the app user account
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to update new app user account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(value = "/updateAppUserAccount", method = RequestMethod.POST)
    public ResponseEntity<?> updateAppUserAccount(@RequestBody AppUserRequest payload) {
        try {
            // user session detail
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.appUserService.updateAppUserAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updateAppUserAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchAllAppUserAccount
     * @apiNote :- Api user to fetch all app user account
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch app user account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(value = "/fetchAllAppUserAccount", method = RequestMethod.POST)
    public ResponseEntity<?> fetchAllAppUserAccount(@RequestBody AppUserRequest payload) {
        try {
            return new ResponseEntity<>(this.appUserService.fetchAllAppUserAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchAllAppUserAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteAppUserAccount
     * @apiNote :- Api use to delete app user account
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to use to delete app user account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(value = "/deleteAppUserAccount", method = RequestMethod.POST)
    public ResponseEntity<?> deleteAppUserAccount(@RequestBody AppUserRequest payload) {
        try {
            // user session detail
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.appUserService.deleteAppUserAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while closeAppUserAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteAllAppUserAccount
     * @apiNote :- Api use to delete all app user account
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to use to delete all app user accounts.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(path="/deleteAllAppUserAccount", method=RequestMethod.POST)
    public ResponseEntity<?> deleteAllAppUserAccount(@RequestBody AppUserRequest payload) {
        try {
            return new ResponseEntity<>(this.appUserService.deleteAllAppUserAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteAllAppUserAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- editAppUserAccount
     * @apiNote :- Api use to edit the app user account
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to enabled and disabled app user account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(value = "/enabledDisabledAppUserAccount", method = RequestMethod.POST)
    public ResponseEntity<?> enabledDisabledAppUserAccount(@RequestBody AppUserRequest payload) {
        try {
            // user session detail
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.appUserService.enabledDisabledAppUserAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while enabledDisabledAppUserAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- downloadAppUserAccount
     * @apiNote :- Api use to download app user account
     * @return ResponseEntity<?> AppUserRequest
     * */
    @ApiOperation(value = "Api use to download app user accounts.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(value = "/downloadAppUserAccount", method = RequestMethod.POST)
    public ResponseEntity<?> downloadAppUserAccount(@RequestBody AppUserRequest payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            DateFormat dateFormat = new SimpleDateFormat(BarcoUtil.SIMPLE_DATE_PATTERN);
            String fileName = "BatchAppUserAccountDownload-"+dateFormat.format(new Date())+"-"+ UUID.randomUUID() + ExcelUtil.XLSX_EXTENSION;
            headers.add(BarcoUtil.CONTENT_DISPOSITION,BarcoUtil.FILE_NAME_HEADER + fileName);
            return ResponseEntity.ok().headers(headers).body(this.appUserService.downloadAppUserAccount(payload).toByteArray());
        } catch (Exception ex) {
            logger.error("An error occurred while downloadAppUserAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
