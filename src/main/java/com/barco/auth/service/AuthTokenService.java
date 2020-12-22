package com.barco.auth.service;

import com.barco.model.dto.JwtAuthenticationRequest;
import com.barco.model.dto.ResponseDTO;

/**
 * @author Nabeel Ahmed
 */
public interface AuthTokenService {

     ResponseDTO login(JwtAuthenticationRequest jwtAuthenticationRequest) throws Exception;
}
