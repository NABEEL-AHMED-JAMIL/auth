package com.barco.auth.service.Impl;

import com.barco.model.dto.*;
import com.barco.model.repository.AppUserRepository;
import com.barco.auth.service.AuthTokenService;
import com.barco.common.security.TokenHelper;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.enums.ApiCode;
import com.barco.model.enums.Status;
import com.barco.model.pojo.AppUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Nabeel Ahmed
 */
@Service
@Transactional
@Scope("prototype")
public class AuthServiceImpl implements AuthTokenService {

    public Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private AppUserRepository appUserRepository;


    public Optional<AppUser> findByUsernameAndStatus(String username, Status status) throws Exception {
        logger.info("Finding AppUser By Username and Status");
        return this.appUserRepository.findByUsernameAndStatus(username, status);
    }

    public Optional<AppUser> findByUsernameAndStatusNot(String username, Status status) throws Exception {
        logger.info("Finding AppUser By Username and Status");
        return this.appUserRepository.findByUsernameIgnoreCaseAndStatusNot(username, status);
    }

    @Override
    public ResponseDTO login(JwtAuthenticationRequest jwtAuthenticationRequest) throws Exception {
        AppUser appUser = null;
        if (BarcoUtil.isValidEmail(jwtAuthenticationRequest.getUsername().trim())) {
            appUser = this.appUserRepository.findByUsernameAndStatusNot(jwtAuthenticationRequest.getUsername().trim(), Status.Delete);
            if (BarcoUtil.isNull(appUser)) {
                return new ResponseDTO(ApiCode.HTTP_404, ApplicationConstants.USER_NOT_FOUND);
            } else if (appUser.getStatus() != Status.Active) {
                if (appUser.getStatus().equals(Status.Pending)) {
                    return new ResponseDTO(ApiCode.PENDING, ApplicationConstants.PENDING_ACCOUNT_MSG);
                } else if (appUser.getStatus().equals(Status.Inactive)) {
                    return new ResponseDTO(ApiCode.INACTIVE_USER, ApplicationConstants.INACTIVE_ACCOUNT);
                } else {
                    return new ResponseDTO(ApiCode.INACTIVE_USER, ApplicationConstants.INACTIVE_ACCOUNT);
                }
            }
        } else {
            return new ResponseDTO(ApiCode.INVALID_EMAIL_PATTREN, ApplicationConstants.INVALID_EMAIL);
        }
        if (BarcoUtil.isNull(appUser)) {
            return new ResponseDTO(ApiCode.HTTP_404, ApplicationConstants.USER_NOT_FOUND);
        }
        LoginTokenDTO loginTokenDTO = new LoginTokenDTO(appUser.getId(), appUser.getUsername(),
                appUser.getFirstName(), appUser.getLastName(), appUser.getUserType());
        String token = this.tokenHelper.generateToken(loginTokenDTO.toString());
        if (!BarcoUtil.isNull(token) && !token.isEmpty()) {
            UserDTO userDTO = setUserResponse(appUser);
            if (!BarcoUtil.isNull(userDTO) && !BarcoUtil.isNull(userDTO.getAppUserId())) {
                userDTO.setToken(token);
            }
            appUser.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG,  userDTO);
        } else {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.UNEXPECTED_ERROR);
        }
    }

    private UserDTO setUserResponse(AppUser appUser) throws Exception {
        UserDTO userDTO = new UserDTO();
        if (!BarcoUtil.isNull(appUser)) {
            if (!BarcoUtil.isNull(appUser.getId())) {
                userDTO.setAppUserId(appUser.getId());
            }
            if (!BarcoUtil.isNull(appUser.getFirstName())) {
                userDTO.setFirstName(appUser.getFirstName());
            }
            if (!BarcoUtil.isNull(appUser.getLastName())) {
                userDTO.setLastName(appUser.getLastName());
            }
            if (!BarcoUtil.isNull(appUser.getUsername())) {
                userDTO.setUsername(appUser.getUsername());
            }
            if (!BarcoUtil.isNull(appUser.getUserType())) {
                userDTO.setUserType(appUser.getUserType());
            }
            if (!BarcoUtil.isNull(appUser.getAuthorities()) && appUser.getAuthorities().size() > 0) {
                List<AuthorityDto> authorityDtos = new ArrayList<>();
                appUser.getAuthorities().forEach(o -> {
                    AuthorityDto authorityDto = new AuthorityDto();
                    authorityDto.setRole(o.getAuthority());
                    authorityDtos.add(authorityDto);
                });
                userDTO.setRoles(authorityDtos);
            }
            if (!BarcoUtil.isNull(appUser.getAccessServices()) && appUser.getAccessServices().size() > 0) {
                Set<AccessServiceDto> accessServiceDtoSet = new HashSet<>();
                appUser.getAccessServices().forEach(accessService -> {
                    AccessServiceDto accessServiceDto = new AccessServiceDto();
                    accessServiceDto.setId(accessService.getId());
                    accessServiceDto.setServiceName(accessService.getServiceName());
                    accessServiceDto.setInternalServiceName(accessService.getInternalServiceName());
                    accessServiceDtoSet.add(accessServiceDto);
                });
                userDTO.setAccessServices(accessServiceDtoSet);
            }
        }
        return userDTO;
    }
}