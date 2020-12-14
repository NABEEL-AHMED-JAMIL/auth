package com.barco.auth.api;

import com.barco.auth.service.AccessServiceService;
import com.barco.auth.service.AuthorityService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.enums.ApiCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = { "Barco-Authority := Barco-Authority EndPoint" })
public class PublicRestApi {

    public Logger logger = LogManager.getLogger(PublicRestApi.class);

    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private AccessServiceService accessServiceService;

    // get all authority q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/getAllAuthority", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get All Authority Rest Api.", notes = "Endpoint help to retrieve all authority")
    public ResponseDTO getAllAuthority() {
        ResponseDTO response = null;
        try {
            logger.info("Request for getAllAuthority");
            // method use to retrieve all authority
            response = this.authorityService.getAllAuthority();
        } catch (Exception ex) {
            logger.info("Error during getAllAuthority " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

    // get all access service q.a pass (11-21-2020)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/getAllAccessService", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get All Authority Rest Api.", notes = "Endpoint help retrieve all access service")
    public ResponseDTO getAllAccessService() {
        ResponseDTO response = null;
        try {
            logger.info("Request for getAllAccessService");
            // method use to access service
            response = this.accessServiceService.getAllAccessService();
        } catch (Exception ex) {
            logger.info("Error during getAllAccessService " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.HTTP_500, ApplicationConstants.UNEXPECTED_ERROR);
        }
        return response;
    }

}

