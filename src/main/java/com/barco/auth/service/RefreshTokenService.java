package com.barco.auth.service;

import com.barco.model.dto.request.TokenRefreshRequest;
import com.barco.model.dto.response.AppResponse;
import com.barco.model.pojo.RefreshToken;
import java.util.Optional;

/**
 * @author Nabeel Ahmed
 */
public interface RefreshTokenService extends RootService {

    public AppResponse fetchSessionStatistics() throws Exception;

    public AppResponse fetchByAllRefreshToken(TokenRefreshRequest payload) throws Exception;

    public Optional<RefreshToken> findByToken(String token) throws Exception;

    public RefreshToken createRefreshToken(Long appUserId, String ip) throws Exception;

    public AppResponse verifyExpiration(RefreshToken payload) throws Exception;

    public AppResponse deleteRefreshToken(TokenRefreshRequest payload) throws Exception;

    public AppResponse deleteAllRefreshToken(TokenRefreshRequest payload) throws Exception;

}