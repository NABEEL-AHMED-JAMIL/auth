package com.barco.auth.controller;

import com.barco.auth.service.EtlAccountService;
import com.barco.common.payload.APIResponse;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Api use to perform crud operation
 * @author Nabeel Ahmed
 */
@RestController
@RequestMapping(value = "/user")
public class EtlAccountRestApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtlAccountRestApi.class);

    private final EtlAccountService etlAccountService;

    public EtlAccountRestApi(EtlAccountService etlAccountService) {
        this.etlAccountService = etlAccountService;
    }

    /**
     * Method to handle requests for retrieving the current user's profile information.
     * It returns the profile details of the currently authenticated user.
     * @return ResponseEntity containing the user's profile information if retrieval is successful,
     * or an error message if retrieval fails.
     * */
    @RequestMapping(value="/me", method= RequestMethod.GET)
    public ResponseEntity<?> getCurrentAccount() {
        LOGGER.info("Received request to get current user's profile information");
        APIResponse<?> apiResponse;
        try {
            apiResponse = this.etlAccountService.getCurrentAccount();
            if (BarcoUtil.isNull(apiResponse)) {
                apiResponse = APIResponse.error("Failed to retrieve user profile");
            }
        } catch (Exception ex) {
            LOGGER.error("Error while retrieving user profile: {}", ex.getMessage(), ex);
            LOGGER.error("Root cause message: {}", ExceptionUtil.getRootCauseMessage(ex));
            apiResponse = APIResponse.error("An error occurred while retrieving user profile: " + ExceptionUtil.getRootCauseMessage(ex));
        }
        return ResponseEntity.status(apiResponse.getReturnCode()).body(apiResponse);
    }


}
