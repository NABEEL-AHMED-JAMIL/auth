package com.barco.auth.service;

import com.barco.model.dto.JwtAuthenticationRequest;
import com.barco.model.dto.ResponseDTO;

public interface AuthTokenService {

     ResponseDTO login(JwtAuthenticationRequest jwtAuthenticationRequest);
}
