package com.barco.auth.service.Impl;


import com.barco.auth.repository.AppUserRepository;
import com.barco.auth.service.AuthTokenService;
import com.barco.common.security.TokenHelper;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.dto.JwtAuthenticationRequest;
import com.barco.model.dto.LoginTokenDTO;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.UserDTO;
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
import java.util.Optional;


@Service
@Transactional
@Scope("prototype")
public class AuthService implements AuthTokenService {

    public Logger logger = LogManager.getLogger(AuthService.class);

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BarcoUtil barcoUtil;


    public Optional<AppUser> findByUsernameAndStatus(String username, Status status) {
        logger.info("Finding AppUser By Username and Status");
        return this.appUserRepository.findByUsernameAndStatus(username, status);
    }

    public Optional<AppUser> findByUsernameAndStatusNot(String username, Status status) {
        logger.info("Finding AppUser By Username and Status");
        return this.appUserRepository.findByUsernameIgnoreCaseAndStatusNot(username, status);
    }

    @Override
    public ResponseDTO login(JwtAuthenticationRequest jwtAuthenticationRequest) {
        AppUser appUser = null;
        if(this.barcoUtil.isValidEmail(jwtAuthenticationRequest.getUsername().trim())) {
            appUser = this.appUserRepository.findByUsernameAndStatusNot(jwtAuthenticationRequest.getUsername().trim(), Status.Delete);
            if(appUser == null) {
                return new ResponseDTO(ApiCode.HTTP_404, ApplicationConstants.USER_NOT_FOUND,  null);
            }
            if (appUser.getStatus() != Status.Active) {
                if (appUser.getStatus().equals(Status.Pending)) {
                    return new ResponseDTO(ApiCode.PENDING, ApplicationConstants.PENDING_ACCOUNT_MSG, null);
                } else if (appUser.getStatus().equals(Status.Inactive)) {
                    return new ResponseDTO(ApiCode.INACTIVE_USER, ApplicationConstants.INACTIVE_ACCOUNT, null);
                } else {
                    return new ResponseDTO(ApiCode.INACTIVE_USER, ApplicationConstants.INACTIVE_ACCOUNT, null);
                }
            }
        } else {
            return new ResponseDTO(ApiCode.INVALID_EMAIL_PATTREN, ApplicationConstants.INVALID_EMAIL, null);
        }
        if(appUser == null) {
            return new ResponseDTO(ApiCode.HTTP_404, ApplicationConstants.USER_NOT_FOUND,  null);
        }
        LoginTokenDTO loginTokenDTO = new LoginTokenDTO(appUser.getId(), appUser.getUsername(),
                appUser.getFirstName(), appUser.getLastName(), appUser.getUserType());
        String token = this.tokenHelper.generateToken(loginTokenDTO.toString());
        if(token != null && !token.isEmpty()) {
            UserDTO userDTO = setUserResponse(appUser);
            if(userDTO != null && userDTO.getAppUserId() != null) { userDTO.setToken(token); }
            appUser.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            return  new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG,  userDTO);
        } else {
            return  new ResponseDTO(ApiCode.ERROR, ApplicationConstants.UNEXPECTED_ERROR);
        }
    }

    private UserDTO setUserResponse(AppUser appUser) {
        UserDTO userDTO = new UserDTO();
        if(appUser != null) {
            userDTO.setAppUserId(appUser.getId());
            userDTO.setFirstName(appUser.getFirstName());
            userDTO.setLastName(appUser.getLastName());
            userDTO.setUsername(appUser.getUsername());
            userDTO.setUserType(appUser.getUserType());
            appUser.getAuthorities().forEach(o -> {
                userDTO.setRole(o.getAuthority());
            });
        }
        return userDTO;
    }
}