package com.barco.auth.service.impl;

import com.barco.auth.service.*;
import com.barco.common.emailer.EmailMessagesFactory;
import com.barco.common.security.JwtUtils;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.dto.request.*;
import com.barco.model.dto.response.AppResponse;
import com.barco.model.dto.response.AuthResponse;
import com.barco.model.pojo.AppUser;
import com.barco.model.pojo.RefreshToken;
import com.barco.model.repository.*;
import com.barco.model.security.UserSessionDetail;
import com.barco.model.util.MessageUtil;
import com.barco.model.util.lookup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nabeel Ahmed
 */
@Service
public class AuthServiceImpl implements AuthService {

    private Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private JwtUtils jwtUtils;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private AppUserRepository appUserRepository;
    private ProfileRepository profileRepository;
    private TemplateRegRepository templateRegRepository;
    private NotificationService notificationService;
    private RefreshTokenService refreshTokenService;
    private LookupDataCacheService lookupDataCacheService;
    private AuthenticationManager authenticationManager;
    private EmailMessagesFactory emailMessagesFactory;

    public AuthServiceImpl(JwtUtils jwtUtils,
        PasswordEncoder passwordEncoder,
        RoleRepository roleRepository,
        AppUserRepository appUserRepository,
        ProfileRepository profileRepository,
        TemplateRegRepository templateRegRepository,
        NotificationService notificationService,
        RefreshTokenService refreshTokenService,
        LookupDataCacheService lookupDataCacheService,
        AuthenticationManager authenticationManager,
        EmailMessagesFactory emailMessagesFactory) {
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.appUserRepository = appUserRepository;
        this.profileRepository = profileRepository;
        this.templateRegRepository = templateRegRepository;
        this.notificationService = notificationService;
        this.refreshTokenService = refreshTokenService;
        this.lookupDataCacheService = lookupDataCacheService;
        this.authenticationManager = authenticationManager;
        this.emailMessagesFactory = emailMessagesFactory;
    }

    /**
     * Method use for signIn appUser
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse signInAppUser(LoginRequest payload) throws Exception {
        logger.info("Request signInAppUser :- {}.", payload);
        Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserSessionDetail userDetails = (UserSessionDetail) authentication.getPrincipal();
        RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(userDetails.getId(), payload.getIpAddress());
        AuthResponse authResponse = new AuthResponse(this.jwtUtils.generateTokenFromUsername(userDetails.getUsername()), refreshToken.getToken());
        authResponse.setIpAddress(payload.getIpAddress());
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.USER_SUCCESSFULLY_AUTHENTICATE, this.getAuthResponseDetail(authResponse, userDetails));
    }

    /**
     * Method use for signUp appUser as user-customer
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse signupAppUser(SignupRequest payload) throws Exception {
        logger.info("Request signupAppUser :- {}.", payload);
        if (BarcoUtil.isNull(payload.getFirstName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.FIRST_NAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getLastName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.LAST_NAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_MISSING);
        } else if (BarcoUtil.isNull(payload.getPassword())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PASSWORD_MISSING);
        } else if (this.appUserRepository.existsByUsername(payload.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_ALREADY_TAKEN);
        } else if (this.appUserRepository.existsByEmail(payload.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_ALREADY_IN_USE);
        }
        AppUser newAppUser = new AppUser();
        newAppUser.setFirstName(payload.getFirstName());
        newAppUser.setLastName(payload.getLastName());
        newAppUser.setEmail(payload.getEmail());
        newAppUser.setUsername(payload.getUsername());
        newAppUser.setImg(payload.getProfileImg());
        newAppUser.setIpAddress(payload.getIpAddress());
        newAppUser.setOrgAccount(Boolean.FALSE);
        newAppUser.setStatus(APPLICATION_STATUS.ACTIVE);
        newAppUser.setPassword(this.passwordEncoder.encode(payload.getPassword()));
        this.roleRepository.findByNameAndStatus(
           this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.DEFAULT_ROLE).getLookupValue(), APPLICATION_STATUS.ACTIVE)
           .ifPresent(role -> newAppUser.setAppUserRoles(Set.of(role)));
        this.profileRepository.findProfileByProfileName(
           this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.DEFAULT_PROFILE).getLookupValue())
           .ifPresent(newAppUser::setProfile);
        newAppUser.setAccountType(ACCOUNT_TYPE.NORMAL);
        this.appUserRepository.save(newAppUser);
        Optional<AppUser> superAdmin = this.appUserRepository.findByUsernameAndStatus(
           this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.ROOT_USER).getLookupValue(), APPLICATION_STATUS.ACTIVE);
        if (superAdmin.isPresent()) {
            this.sendNotification(MessageUtil.REQUESTED_FOR_NEW_ACCOUNT, String.format(MessageUtil.NEW_USER_REGISTER_WITH_ID,
                newAppUser.getUuid()), superAdmin.get(), this.lookupDataCacheService, this.notificationService);
        }
        this.sendRegisterUserEmail(newAppUser, this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.USER_SUCCESSFULLY_REGISTER, newAppUser.getUsername()), payload);
    }

    /**
     * Method use to send email the forgot password
     * @param payload
     * @return AuthResponse
     * @throws Exception
     * */
    @Override
    public AppResponse forgotPassword(ForgotPasswordRequest payload) throws Exception {
        logger.info("Request forgotPassword :- {}.", payload);
        if (BarcoUtil.isNull(payload.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByEmailAndStatus(payload.getEmail(), APPLICATION_STATUS.ACTIVE);
        if (appUser.isPresent()) {
            this.sendForgotPasswordEmail(appUser.get(), this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory, this.jwtUtils);
            this.sendNotification(MessageUtil.FORGOT_PASSWORD, MessageUtil.FORGOT_EMAIL_SEND_TO_YOUR_EMAIL,
                 appUser.get(), this.lookupDataCacheService, this.notificationService);
            return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.EMAIL_SEND_SUCCESSFULLY);
        }
        return new AppResponse(BarcoUtil.ERROR, MessageUtil.ACCOUNT_NOT_EXIST);
    }

    /**
     * Method use to reset app user password
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse resetPassword(PasswordResetRequest payload) throws Exception {
        logger.info("Request resetPassword :- {}.", payload);
        if (BarcoUtil.isNull(payload.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_MISSING);
        } else if (BarcoUtil.isNull(payload.getNewPassword())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PASSWORD_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByEmailAndStatus(payload.getEmail(), APPLICATION_STATUS.ACTIVE);
        if (appUser.isPresent()) {
            appUser.get().setPassword(this.passwordEncoder.encode(payload.getNewPassword()));
            this.appUserRepository.save(appUser.get());
            this.sendResetPasswordEmail(appUser.get(), this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
            this.sendNotification(MessageUtil.RESET_PASSWORD, MessageUtil.RESET_EMAIL_SEND_TO_YOUR_EMAIL,
                 appUser.get(), this.lookupDataCacheService, this.notificationService);
            return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.PASSWORD_RESET_SUCCESSFULLY);
        }
        return new AppResponse(BarcoUtil.ERROR, MessageUtil.ACCOUNT_NOT_EXIST);
    }

    /**
     * Method generate new token base on refresh token
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse authClamByRefreshToken(TokenRefreshRequest payload) throws Exception {
        logger.info("Request authClamByRefreshToken :- {}.", payload);
        if (BarcoUtil.isNull(payload.getRefreshToken())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.REFRESH_TOKEN_MISSING);
        }
        Optional<RefreshToken> refreshToken = this.refreshTokenService.findByToken(payload.getRefreshToken());
        if (refreshToken.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.DATA_NOT_FOUND, MessageUtil.REFRESH_TOKEN));
        }
        AppResponse appResponse = this.refreshTokenService.verifyExpiration(refreshToken.get());
        if (appResponse.getStatus().equals(BarcoUtil.SUCCESS)) {
            payload.setRefreshToken(this.jwtUtils.generateTokenFromUsername(refreshToken.get().getCreatedBy().getUsername()));
        }
        return new AppResponse(appResponse.getStatus(), appResponse.getMessage(), payload);
    }

    /**
     * Method use to delete the token to log Out the session
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse logoutAppUser(TokenRefreshRequest payload) throws Exception {
        logger.info("Request logoutAppUser :- {}.", payload);
        if (BarcoUtil.isNull(payload.getRefreshToken())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.REFRESH_TOKEN_MISSING);
        }
        return this.refreshTokenService.deleteRefreshToken(payload);
    }

    /**
     * Method use to wrap the auth response
     * @param authResponse
     * @param userDetails
     * @return AuthResponse
     * @throws Exception
     * */
    private AuthResponse getAuthResponseDetail(AuthResponse authResponse, UserSessionDetail userDetails) throws Exception {
        authResponse.setUuid(userDetails.getUuid());
        authResponse.setFirstName(userDetails.getFirstName());
        authResponse.setLastName(userDetails.getLastName());
        authResponse.setEmail(userDetails.getEmail());
        authResponse.setUsername(userDetails.getUsername());
        authResponse.setProfileImage(userDetails.getProfileImage());
        authResponse.setIpAddress(userDetails.getIpAddress());
        authResponse.setOrgAccount(userDetails.getOrgAccount());
        authResponse.setRoles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        if (!BarcoUtil.isNull(userDetails.getProfile())) {
            authResponse.setProfile(this.getProfilePermissionResponse(userDetails.getProfile()));
        }
        // account type
        if (!BarcoUtil.isNull(userDetails.getAccountType())) {
            GLookup accountType = GLookup.getGLookup(this.lookupDataCacheService.getChildLookupDataByParentLookupTypeAndChildLookupCode(
                ACCOUNT_TYPE.getName(), userDetails.getAccountType().getLookupCode()));
            authResponse.setAccountType(accountType);
        }
        // organization
        if (!BarcoUtil.isNull(userDetails.getOrganization())) {
            authResponse.setOrganization(this.getOrganizationResponse(userDetails.getOrganization()));
        }
        return authResponse;
    }

}
