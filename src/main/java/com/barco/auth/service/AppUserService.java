package com.barco.auth.service;

import com.barco.model.dto.request.AppUserRequest;
import com.barco.model.dto.request.EnVariablesRequest;
import com.barco.model.dto.request.UpdateUserProfileRequest;
import com.barco.model.dto.response.AppResponse;
import java.io.ByteArrayOutputStream;

/**
 * @author Nabeel Ahmed
 */
public interface AppUserService extends RootService {

    public AppResponse fetchAppUserProfile(String username) throws Exception;

    public AppResponse updateAppUserEnvVariable(EnVariablesRequest payload) throws Exception;

    public AppResponse updateAppUserPassword(UpdateUserProfileRequest payload) throws Exception;

    public AppResponse addAppUserAccount(AppUserRequest payload) throws Exception;

    public AppResponse updateAppUserAccount(AppUserRequest payload) throws Exception;

    public AppResponse fetchAllAppUserAccount(AppUserRequest payload) throws Exception;

    public AppResponse deleteAppUserAccount(AppUserRequest payload) throws Exception;

    public AppResponse deleteAllAppUserAccount(AppUserRequest payload) throws Exception;

    public AppResponse enabledDisabledAppUserAccount(AppUserRequest payload) throws Exception;

    public ByteArrayOutputStream downloadAppUserAccount(AppUserRequest payload) throws Exception;

}
