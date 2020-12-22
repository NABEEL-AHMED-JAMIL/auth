package com.barco.auth.service;

import com.barco.model.dto.AccessServiceDto;
import com.barco.model.dto.ResponseDTO;

/**
 * @author Nabeel Ahmed
 */
public interface AccessServiceService {

    ResponseDTO createAccessService(AccessServiceDto accessService) throws Exception;

    ResponseDTO getAllAccessService() throws Exception;
}
