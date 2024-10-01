package com.barco.auth.service.impl;

import com.barco.auth.service.EventBridgeService;
import com.barco.auth.service.LookupDataCacheService;
import com.barco.auth.service.NotificationService;
import com.barco.auth.service.OrganizationService;
import com.barco.common.emailer.EmailMessagesFactory;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.dto.request.AppUserRequest;
import com.barco.model.dto.request.LinkEBURequest;
import com.barco.model.dto.request.OrganizationRequest;
import com.barco.model.dto.request.SessionUser;
import com.barco.model.dto.response.AppResponse;
import com.barco.model.dto.response.AppUserResponse;
import com.barco.model.dto.response.OrganizationResponse;
import com.barco.model.dto.response.QueryResponse;
import com.barco.model.pojo.*;
import com.barco.model.repository.*;
import com.barco.model.repository.projection.OrganizationProjection;
import com.barco.model.util.MessageUtil;
import com.barco.model.util.lookup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nabeel Ahmed
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {

    private Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ETLCountryRepository etlCountryRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AppUserEnvRepository appUserEnvRepository;
    @Autowired
    private EnvVariablesRepository envVariablesRepository;
    @Autowired
    private EventBridgeRepository eventBridgeRepository;
    @Autowired
    private TemplateRegRepository templateRegRepository;
    @Autowired
    private EventBridgeService eventBridgeService;
    @Autowired
    private LookupDataCacheService lookupDataCacheService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailMessagesFactory emailMessagesFactory;
    @Autowired
    private QueryService queryService;

    public OrganizationServiceImpl() {}

    /**
     * Method use to create the org account
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse addOrgAccount(OrganizationRequest payload) throws Exception {
        logger.info("Request addOrgAccount :- {}.", payload);
        AppResponse validationResponse = this.validateOrgAccountPayload(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        }
        Optional<ETLCountry> etlCountry = this.etlCountryRepository.findByCountryCode(payload.getCountryCode());
        if (etlCountry.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ORG_COUNTRY_CODE_NOT_FOUND);
        }
        // check the access for role and profile for user creating
        AppUserRequest orgAppUserRequest = payload.getUser();
        AppUser orgAppUser = new AppUser();
        orgAppUser.setOrgAccount(Boolean.TRUE); // org account
        orgAppUser.setFirstName(orgAppUserRequest.getFirstName());
        orgAppUser.setLastName(orgAppUserRequest.getLastName());
        orgAppUser.setEmail(orgAppUserRequest.getEmail());
        orgAppUser.setUsername(orgAppUserRequest.getUsername());
        orgAppUser.setImg(orgAppUserRequest.getProfileImg());
        orgAppUser.setIpAddress(orgAppUserRequest.getIpAddress());
        orgAppUser.setPassword(this.passwordEncoder.encode(orgAppUserRequest.getPassword()));
        orgAppUser.setAccountType(ACCOUNT_TYPE.getByLookupCode(orgAppUserRequest.getAccountType()));
        orgAppUser.setStatus(APPLICATION_STATUS.ACTIVE);
        orgAppUser.setOrganization(this.createOrganization(payload));
        // register user role default as admin role
        Set<Role> roleList = this.roleRepository.findAllByNameInAndStatus(orgAppUserRequest.getAssignRole(), APPLICATION_STATUS.ACTIVE);
        if (!roleList.isEmpty()) {
            orgAppUser.setAppUserRoles(roleList);
        }
        // profile
        Optional<Profile> profile = this.profileRepository.findProfileByProfileNameAndStatus(orgAppUserRequest.getProfile(), APPLICATION_STATUS.ACTIVE);
        profile.ifPresent(orgAppUser::setProfile);
        // SESSION USER
        Optional<AppUser> adminUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        orgAppUser.setCreatedBy(adminUser.get());
        orgAppUser.setUpdatedBy(adminUser.get());
        this.appUserRepository.save(orgAppUser);
        // notification & register email
        Optional<AppUser> superAdmin = this.appUserRepository.findByUsernameAndStatus(
            this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.ROOT_USER).getLookupValue(), APPLICATION_STATUS.ACTIVE);
        if (superAdmin.isPresent()) {
            // e-variable for account
            this.envVariablesRepository.findAllByCreatedByAndStatusNotOrderByDateCreatedDesc(superAdmin.get(), APPLICATION_STATUS.DELETE)
            .forEach(envVariables -> {
                this.appUserEnvRepository.save(this.getAppUserEnv(adminUser.get(), orgAppUser, envVariables));
            });
            // event bridge only receiver event bridge if exist and create by the main user
            this.eventBridgeRepository.findAllByBridgeTypeInAndCreatedByAndStatusNotOrderByDateCreatedDesc(
                List.of(EVENT_BRIDGE_TYPE.WEB_HOOK_RECEIVE), superAdmin.get(), APPLICATION_STATUS.DELETE)
            .forEach(eventBridge -> {
                try {
                    LinkEBURequest linkEBURequest = new LinkEBURequest();
                    linkEBURequest.setId(eventBridge.getId());
                    linkEBURequest.setAppUserId(orgAppUser.getId());
                    linkEBURequest.setLinked(Boolean.TRUE);
                    linkEBURequest.setSessionUser(new SessionUser(superAdmin.get().getUsername()));
                    this.eventBridgeService.linkEventBridgeWithUser(linkEBURequest);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        this.sendNotification(MessageUtil.NEW_ORG_ACCOUNT_ADDED, String.format(MessageUtil.NEW_ORG_REGISTER_WITH_ID,
            orgAppUser.getUuid()), adminUser.get(), this.lookupDataCacheService, this.notificationService);
        this.sendRegisterOrgAccountUserEmail(orgAppUser, this.lookupDataCacheService, this.templateRegRepository, this.emailMessagesFactory);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, orgAppUser.getOrganization().getUuid()), payload);
    }

    /**
     * Method use to update the org account
     * Note :- Role and Profile not update in the update org
     * account profile admin and role admin
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updateOrgAccount(OrganizationRequest payload) throws Exception {
        logger.info("Request updateOrgAccount :- {}.", payload);
        AppResponse validationResponse = this.validateOrgAccountPayload(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (!BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ID_MISSING);
        }
        Optional<Organization> organizationOpt = this.organizationRepository.findByUuidAndStatusNot(payload.getUuid(), APPLICATION_STATUS.DELETE);
        if (organizationOpt.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ORG_NOT_FOUND);
        }
        // org app user
        AppUserRequest orgAppUserRequest = payload.getUser();
        Optional<AppUser> orgAppUser = this.appUserRepository.findByUuidAndOrgAccountAndStatusNot(orgAppUserRequest.getUuid(), Boolean.TRUE, APPLICATION_STATUS.DELETE);
        if (orgAppUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        } else if (!orgAppUserRequest.getUsername().equals(orgAppUser.get().getUsername()) && this.appUserRepository.existsByUsername(orgAppUserRequest.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_ALREADY_TAKEN);
        } else if (!orgAppUserRequest.getEmail().equals(orgAppUser.get().getEmail()) && this.appUserRepository.existsByEmail(orgAppUserRequest.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_ALREADY_IN_USE);
        }
        orgAppUser.get().setFirstName(orgAppUserRequest.getFirstName());
        orgAppUser.get().setLastName(orgAppUserRequest.getLastName());
        orgAppUser.get().setEmail(orgAppUserRequest.getEmail());
        orgAppUser.get().setUsername(orgAppUserRequest.getUsername());
        orgAppUser.get().setIpAddress(orgAppUserRequest.getIpAddress());
        orgAppUser.get().setAccountType(ACCOUNT_TYPE.getByLookupCode(orgAppUserRequest.getAccountType()));
        // check the access for role and profile for user creating
        Optional<AppUser> adminUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        adminUser.ifPresent(user -> orgAppUser.get().setUpdatedBy(user));
        orgAppUser.get().setOrganization(this.updateOrganizationFromPayload(organizationOpt.get(), payload));
        this.appUserRepository.save(orgAppUser.get());
        // email send to the admin
        this.sendNotification(MessageUtil.ORG_ACCOUNT_UPDATE, String.format(MessageUtil.ORG_UPDATE_WITH_ID, orgAppUser.get().getUuid()),
            adminUser.get(), this.lookupDataCacheService, this.notificationService);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, payload.getUuid()), payload);
    }

    /**
     * Method use to fetch the all org
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchAllOrgAccount(OrganizationRequest payload) throws Exception {
        logger.info("Request fetchAllOrgAccount :- {}.", payload);
        if (BarcoUtil.isNull(payload.getStartDate())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.START_DATE_MISSING);
        } else if (BarcoUtil.isNull(payload.getEndDate())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.END_DATE_MISSING);
        } else if (BarcoUtil.isNull(payload.getPageNumber())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PAGE_NUMBER_MISSING);
        } else if (BarcoUtil.isNull(payload.getPageSize())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PAGE_SIZE_MISSING);
        }
        Timestamp startDate = Timestamp.valueOf(payload.getStartDate().concat(BarcoUtil.START_DATE));
        Timestamp endDate = Timestamp.valueOf(payload.getEndDate().concat(BarcoUtil.END_DATE));
        // query
        StringBuilder orgQl = new StringBuilder("SELECT new com.barco.model.repository.projection.OrganizationProjection(")
        .append("org.id AS orgId, org.uuid AS orgUuid, org.name AS orgName, org.address AS orgAddress, org.phone AS orgPhone, org.status AS orgStatus, " +
            "org.dateCreated, au.username, au.email, au.accountType) ")
        .append("FROM Organization org ")
        .append("INNER JOIN org.appUser au ")
        .append("WHERE au.status != ").append(APPLICATION_STATUS.DELETE.getLookupCode())
        .append(" AND org.status != ").append(APPLICATION_STATUS.DELETE.getLookupCode())
        .append(" AND au.orgAccount = ").append(Boolean.TRUE)
        .append(" AND org.dateCreated BETWEEN :startDate AND :endDate");
        // Parameters to bind
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", startDate);
        parameters.put("endDate", endDate);
        // Add filters dynamically
        if (!BarcoUtil.isNull(payload.getName())) {
            orgQl.append(" AND LOWER(org.name) LIKE LOWER(CONCAT('%', :orgName, '%')) ");
            parameters.put("orgName", payload.getName());
        }
        if (!BarcoUtil.isNull(payload.getUser()) && !BarcoUtil.isNull(payload.getUser().getEmail())) {
            orgQl.append(" AND LOWER(au.email) LIKE LOWER(CONCAT('%', :email, '%')) ");
            parameters.put("email", payload.getUser().getEmail());
        }
        if (!BarcoUtil.isNull(payload.getUser()) && !BarcoUtil.isNull(payload.getUser().getUsername())) {
            orgQl.append(" AND LOWER(au.username) LIKE LOWER(CONCAT('%', :username, '%')) ");
            parameters.put("username", payload.getUser().getUsername());
        }
        orgQl.append(" ORDER BY org.id DESC");
        Page<OrganizationProjection> response = this.queryService.fetchResultWithPagination(orgQl.toString(), parameters,
            PageRequest.of(payload.getPageNumber(), payload.getPageSize()), OrganizationProjection.class);
        // Optionally, wrap the list in a PageImpl to return paginated response if needed
        Page<OrganizationResponse> updatedResponsePage = new PageImpl<>(response.get()
            .map(organizationProjection -> {
                OrganizationResponse organizationResponse = new OrganizationResponse();
                // Set organization details
                organizationResponse.setUuid(organizationProjection.getOrgUuid());
                organizationResponse.setName(organizationProjection.getOrgName());
                organizationResponse.setAddress(organizationProjection.getOrgAddress());
                organizationResponse.setPhone(organizationProjection.getOrgPhone());
                organizationResponse.setDateCreated((Timestamp) organizationProjection.getDateCreated());
                organizationResponse.setStatus(APPLICATION_STATUS.getStatusByLookupCode(organizationProjection.getOrgStatus().getLookupCode()));
                // Set app user details
                AppUserResponse appUserResponse = new AppUserResponse();
                appUserResponse.setEmail(organizationProjection.getEmail());
                appUserResponse.setUsername(organizationProjection.getUsername());
                // Set account type if present
                if (!BarcoUtil.isNull(organizationProjection.getAccountType())) {
                    GLookup accountType = GLookup.getGLookup(this.lookupDataCacheService.getChildLookupDataByParentLookupTypeAndChildLookupCode(
                        ACCOUNT_TYPE.getName(), organizationProjection.getAccountType().getLookupCode()));
                    appUserResponse.setAccountType(accountType);
                }
                organizationResponse.setOwner(appUserResponse);
                this.fillOrganizationStatistics(organizationProjection, organizationResponse);
                return organizationResponse;
            }).collect(Collectors.toList()), PageRequest.of(payload.getPageNumber(), payload.getPageSize()), response.getTotalElements());
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, updatedResponsePage);
    }

    /**
     * Method use to delete the org by id
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteOrgAccountById(OrganizationRequest payload) throws Exception {
        logger.info("Request deleteOrgAccountById :- {}.", payload);
        if (BarcoUtil.isNull(payload.getId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ID_MISSING);
        }
        // need to call the sp
        Optional<Organization> organization = this.organizationRepository.findByIdAndStatusNot(payload.getId(), APPLICATION_STATUS.DELETE);
        if (organization.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ORG_NOT_FOUND);
        }
        // delete the org and delete the all user and delete the all setting and resource as well
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_DELETED, payload.getId()), payload);
    }

    /**
     * Method use to validate the payload
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    private AppResponse validateOrgAccountPayload(OrganizationRequest payload) throws Exception {
        if (BarcoUtil.isNull(payload.getName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ORG_NAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getPhone())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ORG_PHONE_MISSING);
        } else if (BarcoUtil.isNull(payload.getCountryCode())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ORG_COUNTRY_CODE_MISSING);
        } else if (BarcoUtil.isNull(payload.getAddress())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ORG_ADDRESS_MISSING);
        }   else if (BarcoUtil.isNull(payload.getUser())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USER_MISSING);
        }
        AppUserRequest user = payload.getUser();
        if (BarcoUtil.isNull(user.getFirstName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.FIRST_NAME_MISSING);
        } else if (BarcoUtil.isNull(user.getLastName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.LAST_NAME_MISSING);
        } else if (BarcoUtil.isNull(user.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(user.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_MISSING);
        } else if (BarcoUtil.isNull(user.getPassword())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PASSWORD_MISSING);
        } else if (this.appUserRepository.existsByUsername(user.getUsername())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_ALREADY_TAKEN);
        } else if (this.appUserRepository.existsByEmail(user.getEmail())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.EMAIL_ALREADY_IN_USE);
        } else if (BarcoUtil.isNull(user.getIpAddress())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.IP_ADDRESS_MISSING);
        } else if (BarcoUtil.isNull(user.getAssignRole())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_MISSING);
        } else if (BarcoUtil.isNull(user.getProfile())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_MISSING);
        } else if (BarcoUtil.isNull(user.getAccountType())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ACCOUNT_TYPE_MISSING);
        }
        return (AppResponse) BarcoUtil.NULL;
    }

    /**
     * Method use to create the organization reg
     * @param payload
     * @return Organization
     * @throws Exception
     * */
    private Organization createOrganization(OrganizationRequest payload) throws Exception {
        Optional<AppUser> appUserOpt = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        Organization organization = new Organization();
        organization.setName(payload.getName());
        organization.setPhone(payload.getPhone());
        organization.setAddress(payload.getAddress());
        organization.setCountry(this.etlCountryRepository.findByCountryCode(payload.getCountryCode()).get());
        organization.setCreatedBy(appUserOpt.get());
        organization.setUpdatedBy(appUserOpt.get());
        organization.setStatus(APPLICATION_STATUS.ACTIVE);
        return organization;
    }

    /**
     * Method use to update the org
     * @param organization
     * @param payload
     * @return Organization
     * @throws Exception
     * */
    private Organization updateOrganizationFromPayload(Organization organization, OrganizationRequest payload) throws Exception {
        Optional<AppUser> appUserOpt = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        organization.setName(payload.getName());
        organization.setPhone(payload.getPhone());
        organization.setAddress(payload.getAddress());
        organization.setCountry(this.etlCountryRepository.findByCountryCode(payload.getCountryCode()).get());
        organization.setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
        appUserOpt.ifPresent(organization::setUpdatedBy);
        return organization;
    }

    /**
     * Method use to return organization statistics
     * @param organizationProjection
     * @param organizationResponse
     * */
    private void fillOrganizationStatistics(OrganizationProjection organizationProjection ,OrganizationResponse organizationResponse) {
        // Return the updated OrganizationResponse
        Map<String, Object> orgStatistic = new HashMap<>();
        // APP_SETTING_STATISTICS
        QueryResponse queryResponse;
        String APP_SETTING_STATISTICS = "APP_SETTING_STATISTICS";
        queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.APP_SETTING_STATISTICS, organizationProjection.getOrgId()));
        queryResponse.setQuery((String) BarcoUtil.NULL);
        orgStatistic.put(APP_SETTING_STATISTICS, queryResponse);
        // PROFILE_SETTING_STATISTICS
        String PROFILE_SETTING_STATISTICS = "PROFILE_SETTING_STATISTICS";
        queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.PROFILE_SETTING_STATISTICS, organizationProjection.getOrgId()));
        queryResponse.setQuery((String) BarcoUtil.NULL);
        orgStatistic.put(PROFILE_SETTING_STATISTICS, queryResponse);
        // FORM_SETTING_STATISTICS
        String FORM_SETTING_STATISTICS = "FORM_SETTING_STATISTICS";
        queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.FORM_SETTING_STATISTICS, organizationProjection.getOrgId()));
        queryResponse.setQuery((String) BarcoUtil.NULL);
        orgStatistic.put(FORM_SETTING_STATISTICS, queryResponse);
        // DASHBOARD_AND_REPORT_SETTING_STATISTICS
        String DASHBOARD_AND_REPORT_SETTING_STATISTICS = "DASHBOARD_AND_REPORT_SETTING_STATISTICS";
        queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.DASHBOARD_AND_REPORT_SETTING_STATISTICS, organizationProjection.getOrgId()));
        queryResponse.setQuery((String) BarcoUtil.NULL);
        orgStatistic.put(DASHBOARD_AND_REPORT_SETTING_STATISTICS, queryResponse);
        // SERVICE_SETTING_STATISTICS
        String SERVICE_SETTING_STATISTICS = "SERVICE_SETTING_STATISTICS";
        queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.SERVICE_SETTING_STATISTICS, organizationProjection.getOrgId()));
        queryResponse.setQuery((String) BarcoUtil.NULL);
        orgStatistic.put(SERVICE_SETTING_STATISTICS, queryResponse);
        // SESSION_COUNT_STATISTICS
        String SESSION_COUNT_STATISTICS = "SESSION_COUNT_STATISTICS";
        queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.SESSION_COUNT_STATISTICS, organizationProjection.getOrgId()));
        queryResponse.setQuery((String) BarcoUtil.NULL);
        orgStatistic.put(SESSION_COUNT_STATISTICS, queryResponse);
        // adding org statistic data to each response
        organizationResponse.setOrgStatistic(orgStatistic);
    }

}
