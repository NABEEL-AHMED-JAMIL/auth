package com.barco.auth.service.impl;

import com.barco.auth.payload.response.CurrentAccountResponse;
import com.barco.auth.service.EtlAccountService;
import com.barco.common.payload.APIResponse;
import com.barco.common.security.session.EtlAccountSessionDetail;
import com.barco.common.utility.BarcoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service to expose current authenticated ETL account information.
 * Refactored to safely handle SecurityContext and different principal types.
 * Returns a clear unauthorized response if the user is not authenticated.
 * @author Nabeel Ahmed
 */
@Service
public class EtlAccountServiceImpl implements EtlAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtlAccountServiceImpl.class);

    /**
     * Retrieves the current authenticated user's account information.
     * Safely checks for authentication and principal type before accessing user details.
     * @return APIResponse containing the current account information or an error message if not authenticated.
     */
    @Override
    public APIResponse<?> getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (BarcoUtil.isNull(authentication) || !authentication.isAuthenticated()) {
            LOGGER.warn("getCurrentAccount called but no authenticated user found in SecurityContext");
            return APIResponse.unauthorized("User not authenticated");
        }
        // The principal should be an instance of EtlAccountSessionDetail if authentication was successful
        Object principal = authentication.getPrincipal();
        if (BarcoUtil.isNull(principal)) {
            LOGGER.warn("Authenticated principal is null");
            return APIResponse.unauthorized("User principal is null");
        }
        // Check if the principal is of the expected type before casting
        if (!(principal instanceof EtlAccountSessionDetail)) {
            LOGGER.warn("Unexpected principal type: {}", principal.getClass().getName());
            return APIResponse.unauthorized("Unexpected principal type");
        }
        // Safe to cast now
        EtlAccountSessionDetail etlAccountSessionDetail = (EtlAccountSessionDetail) principal;
        CurrentAccountResponse currentAccountResponse = new CurrentAccountResponse()
            .setUuid(etlAccountSessionDetail.getUuid())
            .setUsername(etlAccountSessionDetail.getUsername())
            .setEmail(etlAccountSessionDetail.getEmail())
            .setActiveProfile(etlAccountSessionDetail.getAccountProfile());
        return APIResponse.success(currentAccountResponse);
    }
}
