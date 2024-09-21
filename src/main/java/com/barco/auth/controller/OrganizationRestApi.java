package com.barco.auth.controller;

import com.barco.auth.service.OrganizationService;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.request.OrganizationRequest;
import com.barco.model.dto.response.AppResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/organization.json")
@Api(value = "Organization Rest Api",
    description = "Organization Service : Service use to create the private org main account [admin role & client type].")
public class OrganizationRestApi extends RootRestApi {

    private Logger logger = LoggerFactory.getLogger(OrganizationRestApi.class);

    @Autowired
    private OrganizationService organizationService;

    /**
     * @apiName :- addOrgAccount
     * @apiNote :- Method use to add org
     * @param payload
     * @return ResponseEntity
     * */
    @ApiOperation(value = "Api use to add the new org account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/addOrgAccount", method= RequestMethod.POST)
    public ResponseEntity<?> addOrgAccount(@RequestBody OrganizationRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.organizationService.addOrgAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while addOrgAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updateOrgAccount
     * @apiNote :- Method use to update org
     * @param payload
     * @return ResponseEntity
     * */
    @ApiOperation(value = "Api use to update the org account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/updateOrgAccount", method= RequestMethod.POST)
    public ResponseEntity<?> updateOrgAccount(@RequestBody OrganizationRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.organizationService.updateOrgAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updateOrgAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchOrgAccountById
     * @apiNote :- Method use to fetch org by id
     * @param payload
     * @return ResponseEntity
     * */
    @ApiOperation(value = "Api use to fetch the org account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/fetchOrgAccountById", method= RequestMethod.POST)
    public ResponseEntity<?> fetchOrgAccountById(@RequestBody OrganizationRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.organizationService.fetchOrgAccountById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchOrgAccountById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchAllOrgAccount
     * @apiNote :- Method use to fetch all org
     * @param payload
     * @return ResponseEntity
     * */
    @ApiOperation(value = "Api use to fetch all the org account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/fetchAllOrgAccount", method= RequestMethod.POST)
    public ResponseEntity<?> fetchAllOrgAccount(@RequestBody OrganizationRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.organizationService.fetchAllOrgAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchAllOrgAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteOrgAccountById
     * @apiNote :- Method use to delete org by id
     * @param payload
     * @return ResponseEntity
     * */
    @ApiOperation(value = "Api use to delete the org account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/deleteOrgAccountById", method= RequestMethod.POST)
    public ResponseEntity<?> deleteOrgAccountById(@RequestBody OrganizationRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.organizationService.deleteOrgAccountById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteOrgAccountById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteAllOrgAccount
     * @apiNote :- Method use to delete all org
     * @param payload
     * @return ResponseEntity
     * */
    @ApiOperation(value = "Api use to delete all the org account.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value="/deleteAllOrgAccount", method= RequestMethod.POST)
    public ResponseEntity<?> deleteAllOrgAccount(@RequestBody OrganizationRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.organizationService.deleteAllOrgAccount(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteAllOrgAccount ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
