package com.barco.auth.service;

import com.barco.auth.service.impl.QueryService;
import com.barco.common.emailer.EmailMessageRequest;
import com.barco.common.emailer.EmailMessagesFactory;
import com.barco.common.emailer.EmailUtil;
import com.barco.common.security.JwtUtils;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.ExceptionUtil;
import com.barco.common.utility.excel.BulkExcel;
import com.barco.common.utility.excel.ExcelUtil;
import com.barco.common.utility.excel.SheetFiled;
import com.barco.model.dto.request.*;
import com.barco.model.dto.response.*;
import com.barco.model.pojo.*;
import com.barco.model.repository.TemplateRegRepository;
import com.barco.model.util.MessageUtil;
import com.barco.model.util.ModelUtil;
import com.barco.model.util.lookup.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import static com.barco.model.util.lookup.EMAIL_TEMPLATE.*;

/**
 * Api use to perform crud operation on root user
 * @author Nabeel Ahmed
 */
public interface RootService {

    Logger logger = LoggerFactory.getLogger(RootService.class);

    /**
     * Method use to fetch the rup-response
     * @param data
     * @return LinkRPUResponse
     * */
    public default LinkRPUResponse getLinkRPUResponse(HashMap<String, Object> data, APPLICATION_STATUS status) {
        LinkRPUResponse linkRPUResponse = new LinkRPUResponse();
        if (data.containsKey(QueryService.ID)) {
            linkRPUResponse.setId(Long.valueOf(data.get(QueryService.ID).toString()));
        }
        if (data.containsKey(QueryService.EMAIL) && !BarcoUtil.isNull(data.get(QueryService.EMAIL))) {
            linkRPUResponse.setEmail(data.get(QueryService.EMAIL).toString());
        }
        if (data.containsKey(QueryService.USERNAME) && !BarcoUtil.isNull(data.get(QueryService.USERNAME))) {
            linkRPUResponse.setUsername(data.get(QueryService.USERNAME).toString());
        }
        if (data.containsKey(QueryService.FULL_NAME) && !BarcoUtil.isNull(data.get(QueryService.FULL_NAME))) {
            linkRPUResponse.setFullName(data.get(QueryService.FULL_NAME).toString());
        }
        if (data.containsKey(QueryService.PROFILE_IMG) && !BarcoUtil.isNull(data.get(QueryService.PROFILE_IMG))) {
            linkRPUResponse.setProfileImg(data.get(QueryService.PROFILE_IMG).toString());
        }
        if (data.containsKey(QueryService.LINK_ID) && !BarcoUtil.isNull(data.get(QueryService.LINK_ID))) {
            linkRPUResponse.setLinkId(Long.valueOf(data.get(QueryService.LINK_ID).toString()));
        }
        if (data.containsKey(QueryService.LINK_DATA) && !BarcoUtil.isNull(data.get(QueryService.LINK_DATA))) {
            linkRPUResponse.setLinkData(data.get(QueryService.LINK_DATA).toString());
        }
        if (data.containsKey(QueryService.LINKED) && !BarcoUtil.isNull(data.get(QueryService.LINKED))) {
            linkRPUResponse.setLinked(Boolean.valueOf(data.get(QueryService.LINKED).toString()));
        }
        if (data.containsKey(QueryService.ENV_VALUE) && !BarcoUtil.isNull(data.get(QueryService.ENV_VALUE))) {
            linkRPUResponse.setEnvValue(data.get(QueryService.ENV_VALUE).toString());
        }
        if (data.containsKey(QueryService.TOKEN_ID) && !BarcoUtil.isNull(data.get(QueryService.TOKEN_ID))) {
            linkRPUResponse.setTokenId(data.get(QueryService.TOKEN_ID).toString());
        }
        if (data.containsKey(QueryService.ACCESS_TOKEN) && !BarcoUtil.isNull(data.get(QueryService.ACCESS_TOKEN))) {
            linkRPUResponse.setAccessToken(data.get(QueryService.ACCESS_TOKEN).toString());
        }
        if (data.containsKey(QueryService.EXPIRE_TIME) && !BarcoUtil.isNull(data.get(QueryService.EXPIRE_TIME))) {
            linkRPUResponse.setExpireTime(data.get(QueryService.EXPIRE_TIME).toString());
        }
        if (data.containsKey(QueryService.LINK_STATUS) && !BarcoUtil.isNull(data.get(QueryService.LINK_STATUS))) {
            linkRPUResponse.setLinkStatus(APPLICATION_STATUS.getStatusByLookupCode(Long.valueOf(data.get(QueryService.LINK_STATUS).toString())));
        } else {
            linkRPUResponse.setLinkStatus(APPLICATION_STATUS.getStatusByLookupCode(status.getLookupCode()));
        }
        linkRPUResponse.setProfile(new ProfileResponse(Long.valueOf(data.get(QueryService.PROFILE_ID).toString()),
            data.get(QueryService.PROFILE_NAME).toString(), data.get(QueryService.DESCRIPTION).toString()));
        return linkRPUResponse;
    }

    /**
     * getAppUserDetail method use to convert entity to dto
     * @param appUser
     * */
    public default AppUserResponse getAppUserDetail(AppUser appUser) {
        AppUserResponse appUserResponse = new AppUserResponse();
        appUserResponse.setUuid(appUser.getUuid());
        appUserResponse.setFirstName(appUser.getFirstName());
        appUserResponse.setLastName(appUser.getLastName());
        appUserResponse.setEmail(appUser.getEmail());
        appUserResponse.setUsername(appUser.getUsername());
        appUserResponse.setProfileImg(appUser.getImg());
        appUserResponse.setIpAddress(appUser.getIpAddress());
        appUserResponse.setOrgAccount(appUser.getOrgAccount());
        appUserResponse.setDateCreated(appUser.getDateCreated());
        appUserResponse.setDateUpdated(appUser.getDateUpdated());
        appUserResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(appUser.getStatus().getLookupType()));
        appUserResponse.setRoles(appUser.getAppUserRoles().stream().map(Role::getName).collect(Collectors.toList()));
        if (!BarcoUtil.isNull(appUser.getProfile())) {
            appUserResponse.setProfile(this.getProfilePermissionResponse(appUser.getProfile()));
        }
        return appUserResponse;
    }

    /**
     * Method use to get the organization response
     * @param organization
     * @return OrganizationResponse
     * */
    public default OrganizationResponse getOrganizationResponse(Organization organization) {
        OrganizationResponse organizationResponse = new OrganizationResponse();
        organizationResponse.setUuid(organization.getUuid());
        organizationResponse.setName(organization.getName());
        organizationResponse.setPhone(organization.getPhone());
        organizationResponse.setAddress(organization.getAddress());
        organizationResponse.setCountry(new ETLCountryResponse(organization.getCountry().getCountryCode(),
            organization.getCountry().getCountryName(), organization.getCountry().getCode()));
        organizationResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(organization.getStatus().getLookupType()));
        organizationResponse.setCreatedBy(getActionUser(organization.getCreatedBy()));
        organizationResponse.setUpdatedBy(getActionUser(organization.getUpdatedBy()));
        organizationResponse.setDateUpdated(organization.getDateUpdated());
        organizationResponse.setDateCreated(organization.getDateCreated());
        return organizationResponse;
    }

    /**
     * getRoleResponse method use to convert entity to dto
     * @param profile
     * */
    public default ProfileResponse getProfilePermissionResponse(Profile profile) {
        ProfileResponse profilePermission = new ProfileResponse();
        profilePermission.setUuid(profile.getUuid());
        profilePermission.setProfileName(profile.getProfileName());
        profilePermission.setDescription(profile.getDescription());
        profilePermission.setPermission(profile.getProfilePermissions().stream()
            .filter(permissionOption -> permissionOption.getStatus().equals(APPLICATION_STATUS.ACTIVE))
            .map(permission -> permission.getPermission().getPermissionName())
            .collect(Collectors.toList()));
        return profilePermission;
    }

    /**
     * Method use to get the action user as response
     * @param appUser
     * */
    public default ActionByUser getActionUser(AppUser appUser) {
        ActionByUser actionByUser = new ActionByUser();
        actionByUser.setUuid(appUser.getUuid());
        actionByUser.setEmail(appUser.getEmail());
        actionByUser.setUsername(appUser.getUsername());
        return actionByUser;
    }

    /**
     * Method use to download template file
     * @param tempStoreDirectory
     * @param bulkExcel
     * @param sheetFiled
     * @return ByteArrayOutputStream
     * @throws Exception
     * */
    public default ByteArrayOutputStream downloadTemplateFile(String tempStoreDirectory,
        BulkExcel bulkExcel, SheetFiled sheetFiled) throws Exception {
        String basePath = tempStoreDirectory + File.separator;
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream inputStream = cl.getResourceAsStream(ExcelUtil.BATCH);
        String fileUploadPath = basePath + System.currentTimeMillis()+ ExcelUtil.XLSX_EXTENSION;
        FileOutputStream fileOut = new FileOutputStream(fileUploadPath);
        assert inputStream != null;
        IOUtils.copy(inputStream, fileOut);
        // after copy the stream into file close
        inputStream.close();
        // 2nd insert data to newly copied file. So that template couldn't be changed.
        XSSFWorkbook workbook = new XSSFWorkbook(new File(fileUploadPath));
        bulkExcel.setWb(workbook);
        XSSFSheet sheet = workbook.createSheet(sheetFiled.getSheetName());
        bulkExcel.setSheet(sheet);
        bulkExcel.fillBulkHeader(0, sheetFiled.getColTitle());
        // Priority
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        File file = new File(fileUploadPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(Files.readAllBytes(file.toPath()));
        file.delete();
        return byteArrayOutputStream;
    }

    /**
     * sendRegisterUser method use on user register.
     *
     * @param appUser
     * @param lookupDataCacheService
     * @param templateRegRepository
     * @param emailMessagesFactory
     */
    public default void sendRegisterUserEmail(AppUser appUser, LookupDataCacheService lookupDataCacheService,
        TemplateRegRepository templateRegRepository, EmailMessagesFactory emailMessagesFactory) {
        try {
            LookupDataResponse senderEmail = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.NON_REPLY_EMAIL_SENDER);
            Optional<TemplateReg> templateReg = templateRegRepository.findFirstByTemplateNameAndStatus(REGISTER_USER.name(), APPLICATION_STATUS.ACTIVE);
            if (templateReg.isEmpty()) {
                logger.error("No Template Found With {}.", REGISTER_USER.name());
                return;
            }
            Map<String, Object> metaData = new HashMap<>();
            metaData.put(EmailUtil.USERNAME, appUser.getUsername());
            metaData.put(EmailUtil.FULL_NAME, appUser.getFirstName().concat(" ").concat(appUser.getLastName()));
            metaData.put(EmailUtil.ROLE, appUser.getAppUserRoles().stream().map(Role::getName).collect(Collectors.joining(",")));
            metaData.put(EmailUtil.PROFILE, appUser.getProfile().getProfileName());
            // email send request
            EmailMessageRequest emailMessageRequest = new EmailMessageRequest();
            emailMessageRequest.setFromEmail(senderEmail.getLookupValue());
            emailMessageRequest.setRecipients(appUser.getEmail());
            emailMessageRequest.setSubject(EmailUtil.USER_REGISTERED);
            emailMessageRequest.setBodyMap(metaData);
            emailMessageRequest.setBodyPayload(templateReg.get().getTemplateContent());
            logger.info("Email Send Status :- {}.", emailMessagesFactory.sendSimpleMailAsync(emailMessageRequest));
        } catch (Exception ex) {
            logger.error("Exception :- {}.", ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    /**
     * sendRegisterUser method use on user register.
     *
     * @param appUser
     * @param lookupDataCacheService
     * @param templateRegRepository
     * @param emailMessagesFactory
     */
    public default void sendRegisterOrgAccountUserEmail(AppUser appUser, LookupDataCacheService lookupDataCacheService,
        TemplateRegRepository templateRegRepository, EmailMessagesFactory emailMessagesFactory) {
        try {
            LookupDataResponse senderEmail = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.NON_REPLY_EMAIL_SENDER);
            Optional<TemplateReg> templateReg = templateRegRepository.findFirstByTemplateNameAndStatus(REGISTER_USER.name(), APPLICATION_STATUS.ACTIVE);
            if (templateReg.isEmpty()) {
                logger.error("No Template Found With {}.", REGISTER_USER.name());
                return;
            }
            Map<String, Object> metaData = new HashMap<>();
            metaData.put(EmailUtil.USERNAME, appUser.getUsername());
            metaData.put(EmailUtil.FULL_NAME, appUser.getFirstName().concat(" ").concat(appUser.getLastName()));
            metaData.put(EmailUtil.ROLE, appUser.getAppUserRoles().stream().map(Role::getName).collect(Collectors.joining(",")));
            metaData.put(EmailUtil.PROFILE, appUser.getProfile().getProfileName());
            metaData.put(EmailUtil.ORG_NAME, appUser.getOrganization().getName());
            metaData.put(EmailUtil.ORG_ADDRESS, appUser.getOrganization().getAddress().concat(",").concat(appUser.getOrganization().getCountry().getCountryName()));
            // email send request
            EmailMessageRequest emailMessageRequest = new EmailMessageRequest();
            emailMessageRequest.setFromEmail(senderEmail.getLookupValue());
            emailMessageRequest.setRecipients(appUser.getEmail());
            emailMessageRequest.setSubject(EmailUtil.ORG_ACCOUNT_REGISTERED);
            emailMessageRequest.setBodyMap(metaData);
            emailMessageRequest.setBodyPayload(templateReg.get().getTemplateContent());
            logger.info("Email Send Status :- {}.", emailMessagesFactory.sendSimpleMailAsync(emailMessageRequest));
        } catch (Exception ex) {
            logger.error("Exception :- {}.", ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    /**
     * sendRegisterUser method use on user register.
     *
     * @param appUser
     * @param lookupDataCacheService
     * @param templateRegRepository
     * @param emailMessagesFactory
     */
    public default void sendEnabledDisabledRegisterUserEmail(AppUser appUser, LookupDataCacheService lookupDataCacheService,
        TemplateRegRepository templateRegRepository, EmailMessagesFactory emailMessagesFactory) {
        try {
            LookupDataResponse senderEmail = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.NON_REPLY_EMAIL_SENDER);
            Optional<TemplateReg> templateReg;
            if (appUser.getStatus().equals(APPLICATION_STATUS.ACTIVE)) {
                templateReg = templateRegRepository.findFirstByTemplateNameAndStatus(ACTIVE_USER_ACCOUNT.name(), APPLICATION_STATUS.ACTIVE);
            } else {
                templateReg = templateRegRepository.findFirstByTemplateNameAndStatus(BLOCK_USER_ACCOUNT.name(), APPLICATION_STATUS.ACTIVE);
            }
            if (templateReg.isEmpty()) {
                logger.error("No Template Found With {}.", (appUser.getStatus().equals(APPLICATION_STATUS.ACTIVE) ? ACTIVE_USER_ACCOUNT.name() : BLOCK_USER_ACCOUNT.name()));
                return;
            }
            Map<String, Object> metaData = new HashMap<>();
            metaData.put(EmailUtil.USERNAME, appUser.getUsername());
            metaData.put(EmailUtil.FULL_NAME, appUser.getFirstName().concat(" ").concat(appUser.getLastName()));
            metaData.put(EmailUtil.ROLE, appUser.getAppUserRoles().stream().map(Role::getName).collect(Collectors.joining(",")));
            metaData.put(EmailUtil.PROFILE, appUser.getProfile().getProfileName());
            // email send request
            EmailMessageRequest emailMessageRequest = new EmailMessageRequest();
            emailMessageRequest.setFromEmail(senderEmail.getLookupValue());
            emailMessageRequest.setRecipients(appUser.getEmail());
            emailMessageRequest.setSubject(appUser.getStatus().equals(APPLICATION_STATUS.ACTIVE) ? EmailUtil.YOUR_ACCOUNT_IS_NOW_ACTIVE : EmailUtil.YOUR_ACCOUNT_HAS_BEEN_BLOCKED);
            emailMessageRequest.setBodyMap(metaData);
            emailMessageRequest.setBodyPayload(templateReg.get().getTemplateContent());
            logger.info("Email Send Status :- {}.", emailMessagesFactory.sendSimpleMailAsync(emailMessageRequest));
        } catch (Exception ex) {
            logger.error("Exception :- {}.", ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    /**
     * sendRegisterUser method use on user register.
     *
     * @param appUser
     * @param lookupDataCacheService
     * @param templateRegRepository
     * @param emailMessagesFactory
     */
    public default void sendForgotPasswordEmail(AppUser appUser, LookupDataCacheService lookupDataCacheService,
        TemplateRegRepository templateRegRepository, EmailMessagesFactory emailMessagesFactory, JwtUtils jwtUtils) {
        try {
            LookupDataResponse senderEmail = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.NON_REPLY_EMAIL_SENDER);
            LookupDataResponse forgotPasswordUrl = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.RESET_PASSWORD_LINK);
            Optional<TemplateReg> templateReg = templateRegRepository.findFirstByTemplateNameAndStatus(FORGOT_USER_PASSWORD.name(), APPLICATION_STATUS.ACTIVE);
            if (templateReg.isEmpty()) {
                logger.error("No Template Found With {}.", FORGOT_USER_PASSWORD.name());
                return;
            }
            // forgot password
            ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
            forgotPasswordRequest.setUuid(appUser.getUuid());
            forgotPasswordRequest.setEmail(appUser.getEmail());
            forgotPasswordRequest.setUsername(appUser.getUsername());
            // meta data
            Map<String, Object> metaData = new HashMap<>();
            metaData.put(EmailUtil.USERNAME, appUser.getUsername());
            metaData.put(EmailUtil.FULL_NAME, appUser.getFirstName().concat(" ").concat(appUser.getLastName()));
            metaData.put(EmailUtil.FORGOT_PASSWORD_URL, forgotPasswordUrl.getLookupValue().concat("?token=")
                .concat(jwtUtils.generateTokenFromUsernameResetPassword(forgotPasswordRequest.toString())));
            // email send request
            EmailMessageRequest emailMessageRequest = new EmailMessageRequest();
            emailMessageRequest.setFromEmail(senderEmail.getLookupValue());
            emailMessageRequest.setRecipients(appUser.getEmail());
            emailMessageRequest.setSubject(EmailUtil.FORGOT_PASSWORD);
            emailMessageRequest.setBodyMap(metaData);
            emailMessageRequest.setBodyPayload(templateReg.get().getTemplateContent());
            logger.info("Email Send Status :- {}.", emailMessagesFactory.sendSimpleMailAsync(emailMessageRequest));
        } catch (Exception ex) {
            logger.error("Exception :- {}.", ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    /**
     * sendResetPassword method use to send reset confirm email
     *
     * @param appUser
     * @param lookupDataCacheService
     * @param templateRegRepository
     * @param emailMessagesFactory
     */
    public default void sendResetPasswordEmail(AppUser appUser, LookupDataCacheService lookupDataCacheService,
        TemplateRegRepository templateRegRepository, EmailMessagesFactory emailMessagesFactory) {
        try {
            LookupDataResponse senderEmail = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.NON_REPLY_EMAIL_SENDER);
            Optional<TemplateReg> templateReg = templateRegRepository.findFirstByTemplateNameAndStatus(RESET_USER_PASSWORD.name(), APPLICATION_STATUS.ACTIVE);
            if (templateReg.isEmpty()) {
                logger.info("No Template Found With {}.", RESET_USER_PASSWORD.name());
                return;
            }
            Map<String, Object> metaData = new HashMap<>();
            metaData.put(EmailUtil.USERNAME, appUser.getUsername());
            metaData.put(EmailUtil.FULL_NAME, appUser.getFirstName().concat(" ").concat(appUser.getLastName()));
            // email send request
            EmailMessageRequest emailMessageRequest = new EmailMessageRequest();
            emailMessageRequest.setFromEmail(senderEmail.getLookupValue());
            emailMessageRequest.setRecipients(appUser.getEmail());
            emailMessageRequest.setSubject(EmailUtil.PASSWORD_UPDATED);
            emailMessageRequest.setBodyMap(metaData);
            emailMessageRequest.setBodyPayload(templateReg.get().getTemplateContent());
            logger.info("Email Send Status :- {}.", emailMessagesFactory.sendSimpleMailAsync(emailMessageRequest));
        } catch (Exception ex) {
            logger.error("Exception :- {}.", ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    /**
     * send close user account email
     *
     * @param appUser
     * @param lookupDataCacheService
     * @param templateRegRepository
     * @param emailMessagesFactory
     */
    public default void sendCloseUserAccountEmail(AppUser appUser, LookupDataCacheService lookupDataCacheService,
        TemplateRegRepository templateRegRepository, EmailMessagesFactory emailMessagesFactory) {
        try {
            LookupDataResponse senderEmail = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.NON_REPLY_EMAIL_SENDER);
            Optional<TemplateReg> templateReg = templateRegRepository.findFirstByTemplateNameAndStatus(DELETE_USER_ACCOUNT.name(), APPLICATION_STATUS.ACTIVE);
            if (templateReg.isEmpty()) {
                logger.info("No Template Found With {}.", RESET_USER_PASSWORD.name());
                return;
            }
            Map<String, Object> metaData = new HashMap<>();
            metaData.put(EmailUtil.USERNAME, appUser.getUsername());
            metaData.put(EmailUtil.FULL_NAME, appUser.getFirstName().concat(" ").concat(appUser.getLastName()));
            // email send request
            EmailMessageRequest emailMessageRequest = new EmailMessageRequest();
            emailMessageRequest.setFromEmail(senderEmail.getLookupValue());
            emailMessageRequest.setRecipients(appUser.getEmail());
            emailMessageRequest.setSubject(EmailUtil.PASSWORD_UPDATED);
            emailMessageRequest.setBodyMap(metaData);
            emailMessageRequest.setBodyPayload(templateReg.get().getTemplateContent());
            logger.info("Email Send Status :- {}.", emailMessagesFactory.sendSimpleMailAsync(emailMessageRequest));
        } catch (Exception ex) {
            logger.error("Exception :- {}.", ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    /**
     * Method use to send notification
     * @param title
     * @param message
     * @param appUser
     * @throws Exception
     * @throws Exception
     * */
    public default void sendNotification(String title, String message, AppUser appUser,
        LookupDataCacheService lookupDataCacheService, NotificationService notificationService) throws Exception {
        LookupDataResponse notificationTime = lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.NOTIFICATION_DISAPPEAR_TIME);
        notificationService.addNotification(new NotificationRequest(new MessageRequest(title, message),
            NOTIFICATION_TYPE.USER_NOTIFICATION.getLookupCode(), ModelUtil.addDays(new Timestamp(System.currentTimeMillis()),
            Long.valueOf(notificationTime.getLookupValue())), NOTIFICATION_STATUS.UNREAD.getLookupCode()), appUser);
    }

    /**
     * Method use to add the link detail
     * @param superAdmin
     * @param role
     * @param appUser
     * @return AppUserRoleAccess
     * */
    public default AppUserRoleAccess getAppUserRoleAccess(AppUser superAdmin, Role role, AppUser appUser) {
        AppUserRoleAccess appUserRoleAccess = new AppUserRoleAccess();
        appUserRoleAccess.setCreatedBy(superAdmin);
        appUserRoleAccess.setUpdatedBy(superAdmin);
        appUserRoleAccess.setRole(role);
        appUserRoleAccess.setAppUser(appUser);
        appUserRoleAccess.setStatus(APPLICATION_STATUS.ACTIVE);
        if (role.getStatus().equals(APPLICATION_STATUS.INACTIVE) || appUser.getStatus().equals(APPLICATION_STATUS.INACTIVE)) {
            appUserRoleAccess.setStatus(APPLICATION_STATUS.INACTIVE);
        }
        return appUserRoleAccess;
    }

    /**
     * Method use to add the link detail
     * @param superAdmin
     * @param profile
     * @param appUser
     * @return AppUserRoleAccess
     * */
    public default AppUserProfileAccess getAppUserProfileAccess(AppUser superAdmin, Profile profile, AppUser appUser) {
        AppUserProfileAccess appUserRoleAccess = new AppUserProfileAccess();
        appUserRoleAccess.setCreatedBy(superAdmin);
        appUserRoleAccess.setUpdatedBy(superAdmin);
        appUserRoleAccess.setProfile(profile);
        appUserRoleAccess.setAppUser(appUser);
        appUserRoleAccess.setStatus(APPLICATION_STATUS.ACTIVE);
        if (profile.getStatus().equals(APPLICATION_STATUS.INACTIVE) || appUser.getStatus().equals(APPLICATION_STATUS.INACTIVE)) {
            appUserRoleAccess.setStatus(APPLICATION_STATUS.INACTIVE);
        }
        return appUserRoleAccess;
    }

    /**
     * Method use to convert the role to role response
     * @param role
     * @return RoleResponse
     * */
    public default RoleResponse gateRoleResponse(Role role) {
        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setUuid(role.getUuid());
        roleResponse.setName(role.getName());
        roleResponse.setDescription(role.getDescription());
        roleResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(role.getStatus().getLookupType()));
        roleResponse.setCreatedBy(getActionUser(role.getCreatedBy()));
        roleResponse.setUpdatedBy(getActionUser(role.getUpdatedBy()));
        roleResponse.setDateUpdated(role.getDateUpdated());
        roleResponse.setDateCreated(role.getDateCreated());
        return roleResponse;
    }

    /**
     * Method use to convert pojo to dto as response
     * @param profile
     * @return ProfileResponse
     * */
    public default ProfileResponse gateProfileResponse(Profile profile) {
        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setUuid(profile.getUuid());
        profileResponse.setProfileName(profile.getProfileName());
        profileResponse.setDescription(profile.getDescription());
        profileResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(profile.getStatus().getLookupType()));
        profileResponse.setCreatedBy(getActionUser(profile.getCreatedBy()));
        profileResponse.setUpdatedBy(getActionUser(profile.getUpdatedBy()));
        profileResponse.setDateUpdated(profile.getDateUpdated());
        profileResponse.setDateCreated(profile.getDateCreated());
        return profileResponse;
    }

    /**
     * Method use to convert pojo to deto as response
     * @param permission
     * @return ProfileResponse
     * */
    public default PermissionResponse gatePermissionResponse(Permission permission) {
        PermissionResponse permissionResponse = new PermissionResponse();
        permissionResponse.setUuid(permission.getUuid());
        permissionResponse.setPermissionName(permission.getPermissionName());
        permissionResponse.setDescription(permission.getDescription());
        permissionResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(permission.getStatus().getLookupType()));
        permissionResponse.setCreatedBy(getActionUser(permission.getCreatedBy()));
        permissionResponse.setUpdatedBy(getActionUser(permission.getUpdatedBy()));
        permissionResponse.setDateUpdated(permission.getDateUpdated());
        permissionResponse.setDateCreated(permission.getDateCreated());
        return permissionResponse;
    }

    /**
     * Method us to get the lookupData
     * @param lookupData
     * */
    public default LookupDataResponse getLookupDataDetail(LookupData lookupData) {
        LookupDataResponse parentLookupData = new LookupDataResponse();
        parentLookupData = this.fillLookupDataResponse(lookupData, parentLookupData, false);
        if (!BarcoUtil.isNull(lookupData.getLookupChildren()) && !lookupData.getLookupChildren().isEmpty()) {
            parentLookupData.setLookupChildren(lookupData.getLookupChildren().stream()
                .map(childLookup -> this.fillLookupDataResponse(childLookup, new LookupDataResponse(), false))
                .collect(Collectors.toSet()));
        }
        return parentLookupData;
    }

    /**
     * Method use to fill the lookup data
     * @param lookupData
     * @param lookupDataResponse
     * */
    public default LookupDataResponse fillLookupDataResponse(LookupData lookupData,
        LookupDataResponse lookupDataResponse, Boolean isFull) {
        lookupDataResponse.setUuid(lookupData.getUuid());
        lookupDataResponse.setLookupCode(lookupData.getLookupCode());
        lookupDataResponse.setLookupValue(lookupData.getLookupValue());
        lookupDataResponse.setLookupType(lookupData.getLookupType());
        if (isFull) {
            lookupDataResponse.setUiLookup(UI_LOOKUP.getStatusByLookupType(lookupData.getUiLookup().getLookupType()));
            lookupDataResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(lookupData.getStatus().getLookupType()));
            lookupDataResponse.setDescription(lookupData.getDescription());
            lookupDataResponse.setCreatedBy(getActionUser(lookupData.getCreatedBy()));
            lookupDataResponse.setUpdatedBy(getActionUser(lookupData.getUpdatedBy()));
            lookupDataResponse.setDateUpdated(lookupData.getDateUpdated());
            lookupDataResponse.setDateCreated(lookupData.getDateCreated());
        }
        return lookupDataResponse;
    }

    /**
     * Method use to enabled or disabled the profile permissions accesses
     * @param appUser
     * @param adminUser
     * */
    public default void enabledDisabledProfilePermissionsAccesses(AppUser appUser, AppUser adminUser) {
        if (!BarcoUtil.isNull(appUser.getProfilePermissionsAccesses()) && !appUser.getProfilePermissionsAccesses().isEmpty()) {
            appUser.getProfilePermissionsAccesses().stream()
            .map(profileAccess -> {
                profileAccess.setStatus(appUser.getStatus());
                profileAccess.setUpdatedBy(adminUser);
                return profileAccess;
            });
        }
    }

    /**
     * Method use to enabled or disabled the app user role accesses
     * @param appUser
     * @param adminUser
     * */
    public default void enabledDisabledAppUserRoleAccesses(AppUser appUser, AppUser adminUser) {
        if (!BarcoUtil.isNull(appUser.getAppUserRoleAccesses()) && !appUser.getAppUserRoleAccesses().isEmpty()) {
            appUser.getAppUserRoleAccesses().stream()
            .map(appUserRoleAccess -> {
                appUserRoleAccess.setStatus(appUser.getStatus());
                appUserRoleAccess.setUpdatedBy(adminUser);
                return appUserRoleAccess;
            });
        }
    }

    /**
     * Method use to enabled and disabled the app user envs
     * @param appUser
     * @param adminUser
     * */
    public default void enabledDisabledAppUserEnvs(AppUser appUser, AppUser adminUser) {
        if (!BarcoUtil.isNull(appUser.getAppUserEnvs()) && !appUser.getAppUserEnvs().isEmpty()) {
            appUser.getAppUserEnvs().stream()
            .map(appUserEnv -> {
                appUserEnv.setStatus(appUser.getStatus());
                appUserEnv.setUpdatedBy(adminUser);
                return appUserEnv;
            });
        }
    }

    /**
     * Method use to enabled and disabled the app user event bridges
     * @param appUser
     * @param adminUser
     * */
    public default void enabledDisabledAppUserEventBridges(AppUser appUser, AppUser adminUser) {
        if (!BarcoUtil.isNull(appUser.getAppUserEventBridges()) && !appUser.getAppUserEventBridges().isEmpty()) {
            appUser.getAppUserEventBridges().stream()
            .map(appUserEventBridge -> {
                appUserEventBridge.setStatus(appUser.getStatus());
                appUserEventBridge.setUpdatedBy(adminUser);
                return appUserEventBridge;
            });
        }
    }

    /**
     * Method use to get app ser env
     * @param superAdmin
     * @param appUser
     * @param envVariables
     * */
    public default AppUserEnv getAppUserEnv(AppUser superAdmin, AppUser appUser, EnvVariables envVariables) {
        AppUserEnv appUserEnv = new AppUserEnv();
        appUserEnv.setCreatedBy(superAdmin);
        appUserEnv.setUpdatedBy(superAdmin);
        appUserEnv.setAppUser(appUser);
        appUserEnv.setEnvVariables(envVariables);
        appUserEnv.setStatus(APPLICATION_STATUS.ACTIVE);
        if (envVariables.getStatus().equals(APPLICATION_STATUS.INACTIVE) || appUser.getStatus().equals(APPLICATION_STATUS.INACTIVE)) {
            appUserEnv.setStatus(APPLICATION_STATUS.INACTIVE);
        }
        return appUserEnv;
    }

    /**
     * Method use to get app ser env
     * @param superAdmin
     * @param appUser
     * @param eventBridge
     * */
    public default AppUserEventBridge getAppUserEventBridge(AppUser superAdmin, AppUser appUser, EventBridge eventBridge) {
        AppUserEventBridge appUserEventBridge = new AppUserEventBridge();
        appUserEventBridge.setCreatedBy(superAdmin);
        appUserEventBridge.setUpdatedBy(superAdmin);
        appUserEventBridge.setAppUser(appUser);
        appUserEventBridge.setEventBridge(eventBridge);
        appUserEventBridge.setTokenId(UUID.randomUUID().toString());
        appUserEventBridge.setStatus(APPLICATION_STATUS.ACTIVE);
        if (eventBridge.getStatus().equals(APPLICATION_STATUS.INACTIVE) || appUser.getStatus().equals(APPLICATION_STATUS.INACTIVE)) {
            appUserEventBridge.setStatus(APPLICATION_STATUS.INACTIVE);
        }
        return appUserEventBridge;
    }

    /**
     * Method use to fetch the refresh token resposne
     * @param refreshToken
     * @return RefreshTokenResponse
     * */
    public default RefreshTokenResponse getRefreshTokenResponse(RefreshToken refreshToken) {
        RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse();
        refreshTokenResponse.setUuid(refreshToken.getUuid());
        refreshTokenResponse.setToken(refreshToken.getToken());
        refreshTokenResponse.setExpiryDate(refreshToken.getExpiryDate());
        refreshTokenResponse.setIpAddress(refreshToken.getIpAddress());
        refreshTokenResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(refreshToken.getStatus().getLookupType()));
        refreshTokenResponse.setCreatedBy(getActionUser(refreshToken.getCreatedBy()));
        refreshTokenResponse.setUpdatedBy(getActionUser(refreshToken.getUpdatedBy()));
        refreshTokenResponse.setDateUpdated(refreshToken.getDateUpdated());
        refreshTokenResponse.setDateCreated(refreshToken.getDateCreated());
        return refreshTokenResponse;
    }

    /**
     * Method use to convert object to EnVariablesResponse
     * @param appUserEnv
     * @return EnVariablesResponse
     * */
    public default EnVariablesResponse getEnVariablesResponse(AppUserEnv appUserEnv) {
        EnVariablesResponse enVariables = new EnVariablesResponse();
        enVariables.setUuid(appUserEnv.getUuid());
        enVariables.setEnvKey(appUserEnv.getEnvVariables().getEnvKey());
        enVariables.setEnvValue(appUserEnv.getEnvValue());
        enVariables.setDescription(appUserEnv.getEnvVariables().getDescription());
        return enVariables;
    }

    /**
     * Method use to get teh event bridge response
     * @param appUserEventBridge
     * @return EventBridgeResponse
     * */
    public default EventBridgeResponse getEventBridgeResponse(AppUserEventBridge appUserEventBridge) {
        EventBridgeResponse eventBridgeResponse = new EventBridgeResponse();
        eventBridgeResponse.setUuid(appUserEventBridge.getUuid());
        eventBridgeResponse.setTokenId(appUserEventBridge.getTokenId());
        eventBridgeResponse.setAccessToken(appUserEventBridge.getAccessToken());
        eventBridgeResponse.setExpireTime(appUserEventBridge.getExpireTime());
        // Event Bridge
        EventBridge eventBridge = appUserEventBridge.getEventBridge();
        eventBridgeResponse.setName(eventBridge.getName());
        eventBridgeResponse.setBridgeUrl(eventBridge.getBridgeUrl());
        eventBridgeResponse.setDescription(eventBridge.getDescription());
        return eventBridgeResponse;
    }

    /**
     * Method use to validate the link EventBridge payload
     * @param payload
     * @return AppResponse
     * */
    public default AppResponse validateLinkEventBridgePayload(LinkEBURequest payload) {
        if (BarcoUtil.isNull(payload.getId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EVENT_BRIDGE_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getAppUserId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APP_USER_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getLinked())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.LINKED_MISSING);
        }
        return (AppResponse) BarcoUtil.NULL;
    }

    /**
     * Method use to create the credential from private key
     * @param credential
     * @return String
     * */
    public default String getCredentialPrivateKey(Credential credential) {
        String credJsonStr = new String(Base64.getDecoder().decode(credential.getContent().getBytes()));
        JsonObject jsonObject = JsonParser.parseString(credJsonStr).getAsJsonObject();
        return jsonObject.get("priKey").getAsString();
    }

    /**
     * Method give one year from today
     * @return Timestamp
     * */
    public default Timestamp getOneYearFromNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        return new Timestamp(calendar.getTimeInMillis());
    }


}
