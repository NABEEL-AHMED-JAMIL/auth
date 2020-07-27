package com.barco.auth.service.Impl;


import com.barco.auth.repository.AuthorityRepository;
import com.barco.auth.service.AuthorityService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.model.dto.AuthorityDto;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.enums.ApiCode;
import com.barco.model.enums.Status;
import com.barco.model.pojo.Authority;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@Scope("prototype")
public class AuthorityServiceImpl implements AuthorityService {

    public Logger logger = LogManager.getLogger(AuthorityServiceImpl.class);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public ResponseDTO createAuthority(AuthorityDto authorityDto) {
        Authority authority = new Authority();
        if(StringUtils.isNotBlank(authorityDto.getRole())) {
            if(this.authorityRepository.findByRoleIgnoreCaseAndStatus(
                    authorityDto.getRole(), Status.Active).isPresent()) {
                return new ResponseDTO(ApiCode.INVALID_REQUEST,
                    ApplicationConstants.AUTHORITY_ALREADY_EXIST, authorityDto);
            }
            authority.setRole(authorityDto.getRole());
        }
        authority.setStatus(Status.Active);
        authority.setCreatedBy(01L);
        // authority save process
        this.authorityRepository.save(authority);
        this.authorityRepository.flush();
        authorityDto.setId(authority.getId());
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, authorityDto);
    }

    @Override
    public ResponseDTO getAllAuthority() {
        List<Authority> authorityList = this.authorityRepository.findAllByStatus(Status.Active);
        List<AuthorityDto> authorityDtos = authorityList.stream().map(authority -> {
            AuthorityDto authorityDto = new AuthorityDto();
            authorityDto.setId(authority.getId());
            authorityDto.setRole(authority.getRole());
            return authorityDto;
        }).collect(Collectors.toList());
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, authorityDtos);
    }

}
