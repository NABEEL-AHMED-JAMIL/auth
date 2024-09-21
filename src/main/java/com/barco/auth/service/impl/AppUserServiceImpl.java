package com.barco.auth.service.impl;

import com.barco.auth.service.AppUserService;
import com.barco.auth.service.EventBridgeService;
import com.barco.auth.service.LookupDataCacheService;
import com.barco.auth.service.NotificationService;
import com.barco.common.emailer.EmailMessagesFactory;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.excel.BulkExcel;
import com.barco.common.utility.excel.ExcelUtil;
import com.barco.common.utility.excel.SheetFiled;
import com.barco.model.dto.request.*;
import com.barco.model.dto.response.AppResponse;
import com.barco.model.dto.response.AppUserResponse;
import com.barco.model.dto.response.EventBridgeResponse;
import com.barco.model.pojo.*;
import com.barco.model.repository.*;
import com.barco.model.util.MessageUtil;
import com.barco.model.util.lookup.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Nabeel Ahmed
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    private Logger logger = LoggerFactory.getLogger(AppUserServiceImpl.class);

    @Value("${storage.efsFileDire}")
    private String tempStoreDirectory;

    @Autowired
    private BulkExcel bulkExcel;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private AppUserEnvRepository appUserEnvRepository;
    @Autowired
    private EnvVariablesRepository envVariablesRepository;
    @Autowired
    private EventBridgeRepository eventBridgeRepository;
    @Autowired
    private TemplateRegRepository templateRegRepository;
    @Autowired
    private SubAppUserRepository subAppUserRepository;
    @Autowired
    private LookupDataCacheService lookupDataCacheService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EventBridgeService eventBridgeService;
    @Autowired
    private EmailMessagesFactory emailMessagesFactory;

    public AppUserServiceImpl() {}

    /**
     * Method use to get appUser detail
     * @param username
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchAppUserProfile(String username) throws Exception {
        logger.info("Request fetchAppUserProfile :- {}.", username);
        if (BarcoUtil.isNull(username)) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(username, APPLICATION_STATUS.ACTIVE);
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        AppUserResponse appUserResponse = this.getAppUserDetail(appUser.get());
        // account type
        if (!BarcoUtil.isNull(appUser.get().getAccountType())) {
            GLookup accountType = GLookup.getGLookup(this.lookupDataCacheService.getChildLookupDataByParentLookupTypeAndChildLookupCode(
                ACCOUNT_TYPE.getName(), appUser.get().getAccountType().getLookupCode()));
            appUserResponse.setAccountType(accountType);
        }
        // organization
        if (!BarcoUtil.isNull(appUser.get().getOrganization())) {
            appUserResponse.setOrganization(this.getOrganizationResponse(appUser.get().getOrganization()));
        }
        // app user evn
        if (!BarcoUtil.isNull(appUser.get().getAppUserEnvs())) {
            appUserResponse.setEnVariables(appUser.get().getAppUserEnvs().stream()
                .filter(appUserEnv -> appUserEnv.getStatus().equals(APPLICATION_STATUS.ACTIVE))
                .map(this::getEnVariablesResponse).collect(Collectors.toList()));
        }
        // app user web hook
        if (!BarcoUtil.isNull(appUser.get().getAppUserEventBridges())) {
            appUserResponse.setEventBridge(appUser.get().getAppUserEventBridges().stream()
                .filter(appUserEventBridge -> appUserEventBridge.getStatus().equals(APPLICATION_STATUS.ACTIVE))
                .map(appUserEventBridge -> {
                    EventBridgeResponse eventBridgeResponse = this.getEventBridgeResponse(appUserEventBridge);
                    // event-bridge type
                    if (!BarcoUtil.isNull(appUserEventBridge.getEventBridge().getBridgeType())) {
                        GLookup bridgeType = GLookup.getGLookup(this.lookupDataCacheService
                            .getChildLookupDataByParentLookupTypeAndChildLookupCode(EVENT_BRIDGE_TYPE.getName(),
                                appUserEventBridge.getEventBridge().getBridgeType().getLookupCode()));
                        eventBridgeResponse.setBridgeType(bridgeType);
                    }
                    // http method
                    if (!BarcoUtil.isNull(appUserEventBridge.getEventBridge().getHttpMethod())) {
                        GLookup httpMethod = GLookup.getGLookup(this.lookupDataCacheService
                            .getChildLookupDataByParentLookupTypeAndChildLookupCode(REQUEST_METHOD.getName(),
                                Long.valueOf(appUserEventBridge.getEventBridge().getHttpMethod().ordinal())));
                        eventBridgeResponse.setHttpMethod(httpMethod);
                    }
                    return eventBridgeResponse;
                }).collect(Collectors.toList()));
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, appUserResponse);
    }

    /**
     * Method use to update app user env variable
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updateAppUserEnvVariable(EnVariablesRequest payload) throws Exception {
        logger.info("Request updateAppUserEnvVariable :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ID_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        Optional<AppUserEnv> appUserEnv = this.appUserEnvRepository.findAppUserEnvByUuidAndAppUser(payload.getUuid(), appUser.get());
        if (appUserEnv.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.ENV_NOT_FOUND_WITH_ID, payload.getUuid()));
        }
        appUserEnv.get().setEnvValue(payload.getEnvValue());
        appUserEnv.get().setUpdatedBy(appUser.get());
        this.appUserEnvRepository.save(appUserEnv.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, payload.getUuid()));
    }

    /**
     * Method use to update app user password
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updateAppUserPassword(UpdateUserProfileRequest payload) throws Exception {
        logger.info("Request updateAppUserPassword :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getOldPassword())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.OLD_PASSWORD_MISSING);
        } else if (BarcoUtil.isNull(payload.getNewPassword())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.NEW_PASSWORD_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getUsername(), APPLICATION_STATUS.ACTIVE);
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        } else if (!this.passwordEncoder.matches(payload.getOldPassword(), appUser.get().getPassword())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.OLD_PASSWORD_NOT_MATCH);
        }
        appUser.get().setPassword(this.passwordEncoder.encode(payload.getNewPassword()));
        this.appUserRepository.save(appUser.get());
        // send to the same user email and notification
        this.sendResetPasswordEmail(appUser.get(), this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
        // notification update password
        this.sendNotification(MessageUtil.PASSWORD_UPDATED, MessageUtil.PASSWORD_UPDATE_MESSAGE, appUser.get(), this.lookupDataCacheService, this.notificationService);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, appUser.get().getUsername()), payload);
    }

    /**
     * Method use to add app user account
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse addAppUserAccount(AppUserRequest payload) throws Exception {
        logger.info("Request addAppUserAccount :- {}.", payload);
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
        } else if (BarcoUtil.isNull(payload.getIpAddress())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.IP_ADDRESS_MISSING);
        } else if (BarcoUtil.isNull(payload.getAssignRole())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_MISSING);
        } else if (BarcoUtil.isNull(payload.getProfile())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_MISSING);
        } else if (BarcoUtil.isNull(payload.getAccountType())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ACCOUNT_TYPE_MISSING);
        }
        Optional<AppUser> adminUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (adminUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        // check the access for role and profile for user creating
        AppUser newAppUser = new AppUser();
        newAppUser.setFirstName(payload.getFirstName());
        newAppUser.setLastName(payload.getLastName());
        newAppUser.setEmail(payload.getEmail());
        newAppUser.setUsername(payload.getUsername());
        newAppUser.setImg(payload.getProfileImg());
        newAppUser.setIpAddress(payload.getIpAddress());
        newAppUser.setStatus(APPLICATION_STATUS.ACTIVE);
        newAppUser.setPassword(this.passwordEncoder.encode(payload.getPassword()));
        newAppUser.setOrganization(adminUser.get().getOrganization()); // linking the same org
        if (!BarcoUtil.isNull(payload.getAccountType())) { // account type
            newAppUser.setAccountType(ACCOUNT_TYPE.getByLookupCode(payload.getAccountType()));
        }
        newAppUser.setCreatedBy(adminUser.get());
        newAppUser.setUpdatedBy(adminUser.get());
        Set<Role> roleList = this.roleRepository.findAllByNameInAndStatus(payload.getAssignRole(), APPLICATION_STATUS.ACTIVE);
        if (!roleList.isEmpty()) {
            newAppUser.setAppUserRoles(roleList);
        }
        Optional<Profile> profile = this.profileRepository.findProfileByProfileNameAndStatus(payload.getProfile(), APPLICATION_STATUS.ACTIVE);
        profile.ifPresent(newAppUser::setProfile);
        this.appUserRepository.save(newAppUser);
        // sub app user detail
        SubAppUser subAppUser = new SubAppUser();
        subAppUser.setAppUserParent(adminUser.get());
        subAppUser.setAppUserChild(newAppUser);
        subAppUser.setCreatedBy(adminUser.get());
        subAppUser.setUpdatedBy(adminUser.get());
        subAppUser.setStatus(APPLICATION_STATUS.ACTIVE);
        this.subAppUserRepository.save(subAppUser);
        // notification & register email
        Optional<AppUser> superAdmin = this.appUserRepository.findByUsernameAndStatus(
            this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.ROOT_USER).getLookupValue(), APPLICATION_STATUS.ACTIVE);
        if (superAdmin.isPresent()) {
            // linking all env variable to the user from the root user
            this.envVariablesRepository.findAllByCreatedByAndStatusNotOrderByDateCreatedDesc(superAdmin.get(), APPLICATION_STATUS.DELETE)
                .forEach(envVariables -> {
                    this.appUserEnvRepository.save(this.getAppUserEnv(adminUser.get(), newAppUser, envVariables));
                });
            // event bridge only receiver event bridge if exist and create by the main user
            this.eventBridgeRepository.findAllByBridgeTypeInAndCreatedByAndStatusNotOrderByDateCreatedDesc(
                List.of(EVENT_BRIDGE_TYPE.WEB_HOOK_RECEIVE), superAdmin.get(), APPLICATION_STATUS.DELETE)
                .forEach(eventBridge -> {
                    try {
                        LinkEBURequest linkEBURequest = new LinkEBURequest();
                        linkEBURequest.setId(eventBridge.getId());
                        linkEBURequest.setAppUserId(newAppUser.getId());
                        linkEBURequest.setLinked(Boolean.TRUE);
                        linkEBURequest.setSessionUser(new SessionUser(superAdmin.get().getUsername()));
                        this.eventBridgeService.linkEventBridgeWithUser(linkEBURequest);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
        }
        // email send to the admin
        this.sendNotification(MessageUtil.NEW_ACCOUNT_ADDED, String.format(MessageUtil.NEW_USER_REGISTER_WITH_ID,
            newAppUser.getUuid()), adminUser.get(), this.lookupDataCacheService, this.notificationService);
        // email send to the user
        this.sendRegisterUserEmail(newAppUser, this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.USER_SUCCESSFULLY_REGISTER, payload.getUsername()), payload);
    }

    /**
     * Method use to edit app user account
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updateAppUserAccount(AppUserRequest payload) throws Exception {
        logger.info("Request updateAppUserAccount :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getFirstName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.FIRST_NAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getLastName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.LAST_NAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_MISSING);
        } else if (BarcoUtil.isNull(payload.getIpAddress())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.IP_ADDRESS_MISSING);
        } else if (BarcoUtil.isNull(payload.getAssignRole())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_MISSING);
        } else if (BarcoUtil.isNull(payload.getProfile())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_MISSING);
        } else if (BarcoUtil.isNull(payload.getAccountType())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ACCOUNT_TYPE_MISSING);
        }
        Optional<AppUser> adminUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (adminUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        // check the access for role and profile for user creating
        Optional<AppUser> updateAppUser = this.appUserRepository.findByUuidAndStatusNot(payload.getUuid(), APPLICATION_STATUS.DELETE);
        if (!BarcoUtil.isNull(payload.getFirstName())) {
            updateAppUser.get().setFirstName(payload.getFirstName());
        }
        if (!BarcoUtil.isNull(payload.getLastName())) {
            updateAppUser.get().setLastName(payload.getLastName());
        }
        if (!BarcoUtil.isNull(payload.getEmail())) {
            updateAppUser.get().setEmail(payload.getEmail());
        }
        if (!BarcoUtil.isNull(payload.getUsername())) {
            updateAppUser.get().setUsername(payload.getUsername());
        }
        if (!BarcoUtil.isNull(payload.getIpAddress())) {
            updateAppUser.get().setIpAddress(payload.getIpAddress());
        }
        if (!BarcoUtil.isNull(payload.getAccountType())) { // account type
            updateAppUser.get().setAccountType(ACCOUNT_TYPE.getByLookupCode(payload.getAccountType()));
        }
        if (!payload.getUsername().equals(updateAppUser.get().getUsername()) && this.appUserRepository.existsByUsername(payload.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_ALREADY_TAKEN);
        } else if (!payload.getEmail().equals(updateAppUser.get().getEmail()) && this.appUserRepository.existsByEmail(payload.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_ALREADY_IN_USE);
        }
        // register user role default as admin role
        Set<Role> roleList = this.roleRepository.findAllByNameInAndStatus(payload.getAssignRole(), APPLICATION_STATUS.ACTIVE);
        if (!roleList.isEmpty()) {
            updateAppUser.get().setAppUserRoles(roleList);
        }
        // profile
        Optional<Profile> profile = this.profileRepository.findProfileByProfileNameAndStatus(payload.getProfile(), APPLICATION_STATUS.ACTIVE);
        profile.ifPresent(value -> updateAppUser.get().setProfile(value));
        adminUser.ifPresent(user -> updateAppUser.get().setUpdatedBy(user));
        this.appUserRepository.save(updateAppUser.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, payload.getUsername()), payload);
    }

    /**
     * Method use to fetch all app user account
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchAllAppUserAccount(AppUserRequest payload) throws Exception {
        logger.info("Request fetchAllAppUserAccount :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getStartDate())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.START_DATE_MISSING);
        } else if (BarcoUtil.isNull(payload.getEndDate())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.END_DATE_MISSING);
        }
        // IF IT'S STAND-ALONE ITS MEAN THESE USER ARE NOT CREATE UNDER ANY USER BUT ITS CAN UPDATE BY SUPER ADMIN ONLY
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        List<AppUser> subUsers;
        Timestamp startDate = Timestamp.valueOf(payload.getStartDate().concat(BarcoUtil.START_DATE));
        Timestamp endDate = Timestamp.valueOf(payload.getEndDate().concat(BarcoUtil.END_DATE));
        if (payload.isStandalone()) {
            subUsers = this.appUserRepository.findAllByDateCreatedBetweenAndStatusNotOrderByDateCreatedDesc(startDate, endDate, APPLICATION_STATUS.DELETE);
        } else {
            subUsers = this.appUserRepository.findAllByDateCreatedBetweenAndAppUserParentAndOrgIdAndStatusNotOrderByDateCreatedDesc(
                startDate, endDate, appUser.get(), appUser.get().getOrganization(), APPLICATION_STATUS.DELETE);
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, subUsers.stream().map(subAppUser -> {
            AppUserResponse appUserResponse = this.getAppUserDetail(subAppUser);
            if (!BarcoUtil.isNull(subAppUser.getAccountType())) {
                GLookup accountType = GLookup.getGLookup(this.lookupDataCacheService.getChildLookupDataByParentLookupTypeAndChildLookupCode(
                    ACCOUNT_TYPE.getName(), subAppUser.getAccountType().getLookupCode()));
                appUserResponse.setAccountType(accountType);
            }
            if (!BarcoUtil.isNull(subAppUser.getSubAppUsers())) {
                appUserResponse.setTotalSubUser(subAppUser.getSubAppUsers().stream()
                    .filter(subAppUser1 -> !subAppUser1.getStatus().equals(APPLICATION_STATUS.DELETE)).count());
            }
            if (!BarcoUtil.isNull(subAppUser.getCreatedBy())) {
                appUserResponse.setCreatedBy(getActionUser(subAppUser.getCreatedBy()));
            }
            if (!BarcoUtil.isNull(subAppUser.getUpdatedBy())) {
                appUserResponse.setUpdatedBy(getActionUser(subAppUser.getUpdatedBy()));
            }
            return appUserResponse;
        }).collect(Collectors.toList()));
    }

    /**
     * Method use to close app user account
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteAppUserAccount(AppUserRequest payload) throws Exception {
        logger.info("Request deleteAppUserAccount :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        }
        Optional<AppUser> adminUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (adminUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        // app user should not delete [active & inactive] user fetch by username
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatusNot(payload.getUsername(), APPLICATION_STATUS.DELETE);
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        appUser.get().setUpdatedBy(adminUser.get());
        appUser.get().setStatus(APPLICATION_STATUS.DELETE);
        this.appUserRepository.save(appUser.get());
        // email to user
        this.sendCloseUserAccountEmail(appUser.get(), this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
        // notification to admin
        this.sendNotification(MessageUtil.ACCOUNT_STATUS, String.format(MessageUtil.ACCOUNT_DELETE_DETAIL,
            appUser.get().getUsername()), adminUser.get(), this.lookupDataCacheService, this.notificationService);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, appUser.get().getUsername()), payload);
    }

    /**
     * Method use to delete app user account
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteAllAppUserAccount(AppUserRequest payload) throws Exception {
        logger.info("Request deleteAllAppUserAccount :- {}.", payload);
        if (BarcoUtil.isNull(payload.getSessionUser().getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        }
        Optional<AppUser> adminUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (adminUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        } else if (BarcoUtil.isNull(payload.getIds())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.IDS_MISSING);
        }
        for (AppUser appUser : this.appUserRepository.findAllById(payload.getIds())) {
            appUser.setUpdatedBy(adminUser.get());
            appUser.setStatus(APPLICATION_STATUS.DELETE);
            this.appUserRepository.save(appUser);
            this.sendCloseUserAccountEmail(appUser, this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
            this.sendNotification(MessageUtil.ACCOUNT_STATUS, String.format(MessageUtil.ACCOUNT_DELETE_DETAIL,
                 appUser.getUsername()), adminUser.get(), this.lookupDataCacheService, this.notificationService);
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_DELETED_ALL, payload);
    }

    /**
     * Method use to enabled and disabled app user account
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse enabledDisabledAppUserAccount(AppUserRequest payload) throws Exception {
        logger.info("Request enabledDisabledAppUserAccount :- {}.", payload);
        if (BarcoUtil.isNull(payload.getSessionUser().getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        }
        Optional<AppUser> adminUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (adminUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getStatus())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPLICATION_STATUS_NOT_FOUND);
        }
        // check the access for role and profile for user creating
        Optional<AppUser> appUser = this.appUserRepository.findByUuidAndStatusNot(payload.getUuid(), APPLICATION_STATUS.DELETE);
        if (!BarcoUtil.isNull(payload.getStatus())) {
            // if status is in-active & delete then we have filter the role and show only those role in user detail
            appUser.get().setUpdatedBy(adminUser.get());
            appUser.get().setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
        }
        this.appUserRepository.save(appUser.get());
        this.sendEnabledDisabledRegisterUserEmail(appUser.get(), this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
        this.sendNotification(MessageUtil.ACCOUNT_STATUS, (appUser.get().getStatus().equals(APPLICATION_STATUS.ACTIVE) ?
                String.format(MessageUtil.ACCOUNT_ENABLED, appUser.get().getUsername()) : String.format(MessageUtil.ACCOUNT_DISABLED,
                appUser.get().getUsername())), adminUser.get(), this.lookupDataCacheService, this.notificationService);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, appUser.get().getUsername()), payload);
    }

    /**
     * Method use to download app user account
     * @param payload
     * @return ByteArrayOutputStream
     * @throws Exception
     * */
    @Override
    public ByteArrayOutputStream downloadAppUserAccount(AppUserRequest payload) throws Exception {
        logger.info("Request downloadAppUserAccount :- {}.", payload);
        if (BarcoUtil.isNull(payload.getSessionUser().getUsername())) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (appUser.isEmpty()) {
            throw new Exception(MessageUtil.APPUSER_NOT_FOUND);
        }
        SheetFiled sheetFiled = this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.APP_USER);
        XSSFWorkbook workbook = new XSSFWorkbook();
        this.bulkExcel.setWb(workbook);
        XSSFSheet xssfSheet = workbook.createSheet(sheetFiled.getSheetName());
        this.bulkExcel.setSheet(xssfSheet);
        AtomicInteger rowCount = new AtomicInteger();
        this.bulkExcel.fillBulkHeader(rowCount.get(), sheetFiled.getColTitle());
        // change to start date and end date filter and if ids include then only download that ids
        Timestamp startDate = Timestamp.valueOf(payload.getStartDate().concat(BarcoUtil.START_DATE));
        Timestamp endDate = Timestamp.valueOf(payload.getEndDate().concat(BarcoUtil.END_DATE));
        Iterator<AppUser> appUserIterator;
        if (!payload.isStandalone()) {
            if (!BarcoUtil.isNull(payload.getIds()) && !payload.getIds().isEmpty()) {
                appUserIterator = this.appUserRepository.findAllByDateCreatedBetweenAndAppUserParentAndOrgIdAndAppUserIdInAndStatusNotOrderByDateCreatedDesc(
                    startDate, endDate, appUser.get(), appUser.get().getOrganization(), payload.getIds(), APPLICATION_STATUS.DELETE).iterator();
            } else {
                appUserIterator = this.appUserRepository.findAllByDateCreatedBetweenAndAppUserParentAndOrgIdAndStatusNotOrderByDateCreatedDesc(
                    startDate, endDate, appUser.get(), appUser.get().getOrganization(), APPLICATION_STATUS.DELETE).iterator();
            }
        } else {
            if (!BarcoUtil.isNull(payload.getIds()) && !payload.getIds().isEmpty()) {
                appUserIterator = this.appUserRepository.findAllByDateCreatedBetweenAndAppUserIdInAndStatusNotOrderByDateCreatedDesc(
                    startDate, endDate, payload.getIds(), APPLICATION_STATUS.DELETE).iterator();
            } else {
                appUserIterator = this.appUserRepository.findAllByDateCreatedBetweenAndStatusNotOrderByDateCreatedDesc(
                    startDate, endDate, APPLICATION_STATUS.DELETE).iterator();
            }
        }
        while (appUserIterator.hasNext()) {
            AppUser appUserItr = appUserIterator.next();
            rowCount.getAndIncrement();
            List<String> dataCellValue = new ArrayList<>();
            dataCellValue.add(appUserItr.getFirstName());
            dataCellValue.add(appUserItr.getLastName());
            dataCellValue.add(appUserItr.getEmail());
            dataCellValue.add(appUserItr.getUsername());
            dataCellValue.add(appUserItr.getIpAddress());
            dataCellValue.add(appUserItr.getProfile().getProfileName());
            dataCellValue.add(appUserItr.getAppUserRoles().stream().map(Role::getName).collect(Collectors.joining(",")));
            this.bulkExcel.fillBulkBody(dataCellValue, rowCount.get());
        }
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        workbook.write(outSteam);
        return outSteam;
    }

    /**
     * Method used to validate the username.
     * @param payload
     * @return AppResponse
     */
    private AppResponse validateUsername(Object payload) {
        SessionUser sessionUser = null;
        // Check if the payload is an instance of RoleRequest or other types
        if (payload instanceof AppUserRequest) {
            AppUserRequest appUserRequest = (AppUserRequest) payload;
            sessionUser = appUserRequest.getSessionUser();
        } else {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.INVALID_PAYLOAD_TYPE);
        }
        // Ensure sessionUser is not null
        if (BarcoUtil.isNull(sessionUser)) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.SESSION_USER_MISSING);
        } else if (BarcoUtil.isNull(sessionUser.getUsername())) {
            // Check if the username is null or empty
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        } else if (this.appUserRepository.findByUsernameAndStatus(sessionUser.getUsername(), APPLICATION_STATUS.ACTIVE).isEmpty()) {
            // Check if the username exists and has an active status
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        // Username is valid
        return (AppResponse) BarcoUtil.NULL;
    }

}
