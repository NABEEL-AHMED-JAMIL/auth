package com.barco.auth.controller;

import com.barco.auth.service.RefreshTokenService;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.request.TokenRefreshRequest;
import com.barco.model.dto.response.AppResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Api use to perform crud operation
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins="*")
@RequestMapping(value="/refreshToken.json")
@Api(value = "Refresh Token Rest Api",
    description = "Refresh Token Service : Service related to the [Session & Token Regenerate] for user.")
public class RefreshTokenRestApi {

    private Logger logger = LoggerFactory.getLogger(RefreshTokenRestApi.class);

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * @apiName :- fetchSessionStatistics
     * @apiNote :- Method use to fetch the session statistics
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch session statistics for all user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/fetchSessionStatistics", method=RequestMethod.GET)
    public ResponseEntity<?> fetchSessionStatistics() {
        try {
            return new ResponseEntity<>(this.refreshTokenService.fetchSessionStatistics(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchSessionStatistics ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchByAllRefreshToken
     * @apiNote :- Method use to fetch the data
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch all refresh token for all users.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/fetchByAllRefreshToken", method=RequestMethod.POST)
    public ResponseEntity<?> fetchByAllRefreshToken(@RequestBody TokenRefreshRequest payload) {
        try {
            return new ResponseEntity<>(this.refreshTokenService.fetchByAllRefreshToken(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchByAllRefreshToken ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteRefreshToken
     * @apiNote :- Api use to delete refresh token
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete refresh token for user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/deleteRefreshToken", method=RequestMethod.POST)
    public ResponseEntity<?> deleteRefreshToken(@RequestBody TokenRefreshRequest payload) {
        try {
            return new ResponseEntity<>(this.refreshTokenService.deleteRefreshToken(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteRefreshToken ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteAllRefreshToken
     * @apiNote :- Api use to delete all refresh token
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete all refresh token for users.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/deleteAllRefreshToken", method=RequestMethod.POST)
    public ResponseEntity<?> deleteAllRefreshToken(@RequestBody TokenRefreshRequest payload) {
        try {
            return new ResponseEntity<>(this.refreshTokenService.deleteAllRefreshToken(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteAllRefreshToken ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
