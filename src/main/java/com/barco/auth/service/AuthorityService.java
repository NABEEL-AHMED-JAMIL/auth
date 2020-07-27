package com.barco.auth.service;


import com.barco.model.dto.AuthorityDto;
import com.barco.model.dto.ResponseDTO;

public interface AuthorityService {

    ResponseDTO createAuthority(AuthorityDto authority);

    ResponseDTO getAllAuthority();

}
