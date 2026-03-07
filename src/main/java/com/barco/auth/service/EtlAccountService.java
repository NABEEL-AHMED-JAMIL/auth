package com.barco.auth.service;

import com.barco.common.payload.APIResponse;

/**
 * @author Nabeel Ahmed
 */
public interface EtlAccountService {

    public APIResponse<?> getCurrentAccount();

}
