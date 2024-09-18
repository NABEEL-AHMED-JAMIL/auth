package com.barco.auth.service;

import com.barco.model.dto.request.OrganizationRequest;
import com.barco.model.dto.response.AppResponse;

/**
 * @author Nabeel Ahmed
 */
public interface OrganizationService extends RootService {

    public AppResponse addOrgAccount(OrganizationRequest payload) throws Exception;

    public AppResponse updateOrgAccount(OrganizationRequest payload) throws Exception;

    public AppResponse fetchOrgAccountById(OrganizationRequest payload) throws Exception;

    public AppResponse fetchAllOrgAccount(OrganizationRequest payload) throws Exception;

    public AppResponse deleteOrgAccountById(OrganizationRequest payload) throws Exception;

    public AppResponse deleteAllOrgAccount(OrganizationRequest payload) throws Exception;

}
