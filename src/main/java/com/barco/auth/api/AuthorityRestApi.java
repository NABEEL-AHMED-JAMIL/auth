package com.barco.auth.api;

import com.barco.auth.service.AuthorityService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.AuthorityDto;
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


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/public.json", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = { "Barco-Cron-Authority := Barco-Cron-Authority EndPoint" })
public class AuthorityRestApi {

    public Logger logger = LogManager.getLogger(AuthorityRestApi.class);

    @Autowired
    private AuthorityService authorityService;

    // create authority
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/createAuthority", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create Authority.", notes = "This method use to create the role for user.")
    public @ResponseBody ResponseDTO createAuthority(@RequestBody AuthorityDto authority) {
        ResponseDTO response = null;
        try {
            logger.info("Request for createAuthority " + authority);
            // method use to create new authority
            response = this.authorityService.createAuthority(authority);
        } catch (Exception ex) {
            logger.info("Error during createAuthority " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.ERROR,
                    ApplicationConstants.UNEXPECTED_ERROR + ex.getMessage(), ApiCode.HTTP_500);
        }
        return response;
    }

    // get all authority
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/getAllAuthority", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get All Authority.", notes = "Retrieve all authority")
    public @ResponseBody ResponseDTO getAllAuthority() {
        ResponseDTO response = null;
        try {
            logger.info("Request for getAllAuthority");
            // method use to retrieve all authority
            response = this.authorityService.getAllAuthority();
        } catch (Exception ex) {
            logger.info("Error during getAllAuthority " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO (ApiCode.ERROR,
                    ApplicationConstants.UNEXPECTED_ERROR + ex.getMessage(), ApiCode.HTTP_500);
        }
        return response;
    }

}

