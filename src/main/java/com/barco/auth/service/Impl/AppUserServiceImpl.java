package com.barco.auth.service.Impl;


import com.barco.auth.repository.AccessServiceRepository;
import com.barco.model.dto.*;
import com.barco.model.enums.KeyType;
import com.barco.model.enums.UserType;
import com.barco.model.pojo.*;
import com.barco.model.repository.AppUserRepository;
import com.barco.auth.repository.AuthorityRepository;
import com.barco.auth.repository.UserVerificationRepository;
import com.barco.auth.service.AppUserService;
import com.barco.common.emailer.EmailMessagesFactory;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.TimeUtil;
import com.barco.model.enums.ApiCode;
import com.barco.model.enums.Status;
import com.barco.model.repository.NotificationClientRepository;
import com.barco.model.service.QueryServices;
import com.barco.model.util.PagingUtil;
import com.barco.model.util.QueryUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Nabeel Ahmed
 */
@Service
@Transactional
@Scope("prototype")
public class AppUserServiceImpl implements AppUserService {

    public Logger logger = LogManager.getLogger(AppUserServiceImpl.class);

    private String EMAIL = "email";
    private String FULLNAME = "fullName";
    private String TOKEN = "token";

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private AccessServiceRepository accessServiceRepository;
    @Autowired
    private NotificationClientRepository notificationClientRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserVerificationRepository userVerificationRepository;
    @Autowired
    private EmailMessagesFactory emailMessagesFactory;
    @Autowired
    private QueryUtil queryUtil;
    @Autowired
    private QueryServices queryServices;


    @Override
    public ResponseDTO saveUserRegistration(UserDTO userDTO) throws Exception {
        ResponseDTO saveUserValidation = this.validation(userDTO);
        if (!BarcoUtil.isNull(saveUserValidation)) {
            return saveUserValidation;
        }
        // save app user detail
        AppUser appUser = saveUserDetail(userDTO);
        // notification-client-detail
        this.saveNotificationClientDetail(userDTO, appUser);
        String token = this.getToken();
        this.saveUserVerification(appUser, token);
        // user id set back for dto
        userDTO.setAppUserId(appUser.getId());
        userDTO.setToken(token);
        this.saveUserRegistrationEmailDetail(userDTO);
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, userDTO);
    }

    @Override
    public ResponseDTO saveUserRegistrationByAdmin(UserDTO userDTO) throws Exception {
        if(!BarcoUtil.isNull(userDTO) && BarcoUtil.isNull(userDTO.getAppUserId())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.ADMIN_USER_DETAIL_MISSING);
        }
        ResponseDTO saveUserValidation = this.validation(userDTO);
        if (!BarcoUtil.isNull(saveUserValidation)) {
            return saveUserValidation;
        }
        // save app user detail
        AppUser appUser = saveUserDetail(userDTO);
        // notification-client-detail
        this.saveNotificationClientDetail(userDTO, appUser);
        String token = this.getToken();
        this.saveUserVerification(appUser, token);
        AppUser adminUser = this.appUserRepository.findById(userDTO.getAppUserId()).get();
        Set<AppUser> subUsers = adminUser.getSubUser();
        if (!BarcoUtil.isNull(subUsers)) {
            subUsers.add(appUser);
        } else {
            subUsers = new HashSet<>();
            subUsers.add(appUser);
        }
        adminUser.setSubUser(subUsers);
        // save the detail of admin user
        this.appUserRepository.save(adminUser);
        // user id set back for dto
        userDTO.setAppUserId(appUser.getId());
        userDTO.setToken(token);
        this.saveUserRegistrationEmailDetail(userDTO);
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, userDTO);
    }

    @Override
    public ResponseDTO emailTokenVerification(String token) throws Exception {
        UserVerification userVerification = this.userVerificationRepository.findByTokenAndStatus(token, Status.Active);
        if (ObjectUtils.isEmpty(userVerification)) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.REQUEST_CANNOT_BE_PROCESSED);
        }
        AppUser appUser = this.appUserRepository.findByIdAndStatusNot(userVerification.getCreatedBy(), Status.Delete);
        if (appUser.getStatus().value == Status.Active.value) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.ACCOUNT_ALREADY_ACTIVATED);
        }
        // notification detail save
        NotificationClient notificationClient = this.notificationClientRepository.findByCreatedBy(appUser.getId());
        if (!BarcoUtil.isNull(notificationClient)) {
            notificationClient.setStatus(Status.Active);
            notificationClient.setModifiedBy(appUser.getId());
            notificationClient.setModifiedAt(new Timestamp(System.currentTimeMillis()));
            this.notificationClientRepository.saveAndFlush(notificationClient);
        }
        // verification detail activated time
        userVerification.setStatus(Status.Delete);
        userVerification.setModifiedBy(appUser.getId());
        userVerification.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        userVerification.setActivatedAt(new Timestamp(System.currentTimeMillis()));
        userVerification.setConsumed(true);
        this.userVerificationRepository.saveAndFlush(userVerification);
        // user detail
        appUser.setStatus(Status.Active);
        appUser.setModifiedBy(appUser.getId());
        appUser.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        this.appUserRepository.saveAndFlush(appUser);
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.ACCOUNT_SUCCESSFULLY_ACTIVATED);
    }

    @Override
    public ResponseDTO forgetPassword(String email) throws Exception {
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(email.toLowerCase().trim(), Status.Active);
        if (!appUser.isPresent()) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.USER_ID_NOT_EXIST);
        }
        String token = this.getToken();
        UserVerification userVerification = new UserVerification();
        userVerification.setExpiryDate(TimeUtil.addHoursInTimeStamp(new Timestamp(System.currentTimeMillis()), 24));
        userVerification.setCreatedBy(appUser.get().getId());
        userVerification.setStatus(Status.Active);
        userVerification.setConsumed(false);
        userVerification.setToken(token);
        this.userVerificationRepository.saveAndFlush(userVerification);
        this.forgetPasswordEmailDetail(appUser.get(), token);
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.PASSWORD_RESET_EMAIL_MESSAGE);
    }

    @Override
    public ResponseDTO resetPassword(UserDTO userDTO) throws Exception {
        UserVerification userVerification = this.userVerificationRepository.findByTokenAndStatus(userDTO.getToken(), Status.Active);
        if (ObjectUtils.isEmpty(userVerification)) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.REQUEST_CANNOT_BE_PROCESSED);
        } else {
            Optional<AppUser> appUser = this.appUserRepository.findByIdAndStatus(userVerification.getCreatedBy(), Status.Active);
            if (!appUser.isPresent()) {
                return new ResponseDTO(ApiCode.HTTP_404, ApplicationConstants.USER_ID_NOT_EXIST);
            }
            if (userVerification.getConsumed()) {
                return new ResponseDTO(ApiCode.ALREADY_CONSUMED, ApplicationConstants.ALREADY_RESET_PASSWORD);
            }
            //checking expiry date which is 24 hours
            Timestamp currentDate = new Timestamp(new Date().getTime());
            if (currentDate.getTime() > userVerification.getExpiryDate().getTime()) {
                return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.TOKEN_EXPIRE);
            }
            if(StringUtils.isNotBlank(userDTO.getPassword())) {
                appUser.get().setPassword(this.passwordEncoder.encode(userDTO.getPassword()));
                appUser.get().setModifiedBy(appUser.get().getId());
            }
            userVerification.setStatus(Status.Delete);
            userVerification.setModifiedBy(appUser.get().getId());
            userVerification.setPasswordAdded(true);
            userVerification.setModifiedAt(new Timestamp(System.currentTimeMillis()));
            userVerification.setActivatedAt(new Timestamp(System.currentTimeMillis()));
            userVerification.setConsumed(true);
            // app user repo
            this.appUserRepository.saveAndFlush(appUser.get());
            // user verification repo
            this.userVerificationRepository.saveAndFlush(userVerification);
            return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.PASSWORD_RESET_SUCCESS);

        }
    }

    @Override
    public ResponseDTO fetchSuperAdminUserList(Long superAdminId) throws Exception {
        List<Object[]> fetchSuperAdminUserListQueryResponse = this.queryServices.executeQuery(
            this.queryUtil.fetchSuperAdminUserListQuery(superAdminId));
        if (fetchSuperAdminUserListQueryResponse != null && fetchSuperAdminUserListQueryResponse.size() > 0) {
            List<SuperAdminUserListDto> superAdminUserListDtos = new ArrayList<>();
            for (Object[] object: fetchSuperAdminUserListQueryResponse) {
                SuperAdminUserListDto adminUserList = new SuperAdminUserListDto();
                if (!BarcoUtil.isNull(object[0])) {
                    adminUserList.setId(Long.valueOf(object[0].toString()));
                }
                if (!BarcoUtil.isNull(object[1])) {
                    adminUserList.setUsername(object[1].toString());
                }
                if (!BarcoUtil.isNull(object[2])) {
                    adminUserList.setRole(object[2].toString());
                }
                List<Object[]> fetchSuperAdminAccessServiceResponse = this.queryServices.executeQuery(
                        String.format(this.queryUtil.fetchSuperAdminAccessService(), adminUserList.getId()));
                if (fetchSuperAdminAccessServiceResponse != null && fetchSuperAdminAccessServiceResponse.size() > 0) {
                    Set<AccessServiceDto> accessServices = new HashSet<>();
                    for (Object[] object1: fetchSuperAdminAccessServiceResponse) {
                        AccessServiceDto accessServiceDto = new AccessServiceDto();
                        if (!BarcoUtil.isNull(object1[0])) {
                            accessServiceDto.setId(Long.valueOf(object1[0].toString()));
                        }
                        if (!BarcoUtil.isNull(object1[1])) {
                            accessServiceDto.setServiceName(object1[1].toString());
                        }
                        accessServices.add(accessServiceDto);
                    }
                    adminUserList.setAccessServices(accessServices);
                }
                superAdminUserListDtos.add(adminUserList);
            }
            return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, superAdminUserListDtos);
        }
        return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.HTTP_404_MSG);
    }


    @Override
    public ResponseDTO findAllAdminUsersInPagination(Pageable paging, Long adminId, SearchTextDto searchTextDto,
         String startDate, String endDate, String order, String columnName) {
        ResponseDTO responseDTO = null;
        Object countQueryResult = this.queryServices.executeQueryForSingleResult(
                this.queryUtil.adminUsersList(true, adminId, startDate, endDate, searchTextDto));
        if (!BarcoUtil.isNull(countQueryResult)) {
            /* fetch Record According to Pagination*/
            List<Object[]> result = this.queryServices.executeQuery(
                    this.queryUtil.adminUsersList(false, adminId, startDate, endDate, searchTextDto), paging);
            if (!BarcoUtil.isNull(result) && result.size() > 0) {
                List<UserListingDto> userListingDtos = new ArrayList<>();
                for(Object[] obj : result) {
                    UserListingDto userListingDto = new UserListingDto();
                    if (!BarcoUtil.isNull(obj[0])) {
                        userListingDto.setId(new Long(obj[0].toString()));
                    }
                    if (!BarcoUtil.isNull(obj[1])) {
                        userListingDto.setCreatedAt(Timestamp.valueOf(obj[1].toString()));
                    }
                    if (!BarcoUtil.isNull(obj[2])) {
                        userListingDto.setFullName(obj[2].toString());;
                    }
                    if (!BarcoUtil.isNull(obj[3])) {
                        userListingDto.setUsername(obj[3].toString());
                    }
                    if (!BarcoUtil.isNull(obj[4])) {
                        userListingDto.setLastLoginAt(Timestamp.valueOf(obj[4].toString()));
                    }
                    if (!BarcoUtil.isNull(obj[5])) {
                        userListingDto.setRole(obj[5].toString());
                    }
                    if (!BarcoUtil.isNull(obj[6])) {
                        userListingDto.setStatus(Status.getStatus(new Long(obj[6].toString())));
                    }
                    if (!BarcoUtil.isNull(obj[7])) {
                        userListingDto.setUserType(UserType.getUserType(new Long(obj[7].toString())));
                    }
                    userListingDtos.add(userListingDto);
                }
                responseDTO = new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, userListingDtos,
                        PagingUtil.convertEntityToPagingDTO(Long.valueOf(countQueryResult.toString()), paging));
            }
        } else {
            responseDTO = new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, new ArrayList<>());
        }
        return responseDTO;
    }

    private AppUser saveUserDetail(UserDTO userDTO) throws Exception {
        // save detail into db
        AppUser appUser = new AppUser();
        // first name
        if (StringUtils.isNotEmpty(userDTO.getFirstName())) {
            appUser.setFirstName(userDTO.getFirstName());
        }
        // last name
        if (StringUtils.isNotEmpty(userDTO.getLastName())) {
            appUser.setLastName(userDTO.getLastName());
        }
        // user name
        if (StringUtils.isNotBlank(userDTO.getUsername())) {
            appUser.setUsername(userDTO.getUsername());
        }
        // password
        if (StringUtils.isNotEmpty(userDTO.getPassword())) {
            appUser.setPassword(this.passwordEncoder.encode(userDTO.getPassword()));
        }
        // role from db
        List<Authority> authorities = new ArrayList<>();
        userDTO.getRoles().stream().forEach(accessServiceDto -> {
            Optional<Authority> authority = this.authorityRepository.findByRole(accessServiceDto.getRole());
            if (authority.isPresent()) {
                authorities.add(authority.get());
            }
        });
        appUser.setAuthorities(authorities);
        if (!BarcoUtil.isNull(userDTO.getAccessServices()) && userDTO.getAccessServices().size() > 0) {
            appUser.setAccessServices(this.accessServiceRepository.findAllByIdInAndStatus(userDTO.getAccessServices()
                .stream().map(accessServiceDto -> {
                    return accessServiceDto.getId();
                }).collect(Collectors.toList()), Status.Active));
        }
        appUser.setUserType(userDTO.getUserType());
        appUser.setStatus(Status.Pending);
        // if appUserId there its mean its (Admin | Super-Admin)
        if (!BarcoUtil.isNull(userDTO.getAppUserId())) {
            // modify not added bz its update by the real user not by the create user
            appUser.setCreatedBy(userDTO.getAppUserId());
        }
        // save user to db
        this.appUserRepository.saveAndFlush(appUser);
        return appUser;
    }

    private void saveNotificationClientDetail(UserDTO userDTO, AppUser appUser) throws Exception {
        // save the notification detail
        NotificationClient notificationClient = new NotificationClient();
        // client path
        if (StringUtils.isNotEmpty(userDTO.getClientPath())) {
            notificationClient.setClientPath(userDTO.getClientPath());
        }
        // topic id
        if (StringUtils.isNotEmpty(userDTO.getTopicId())) {
            notificationClient.setTopicId(userDTO.getTopicId());
        }
        notificationClient.setCreatedBy(appUser.getId());
        notificationClient.setStatus(Status.Pending);
        // save notification client
        this.notificationClientRepository.saveAndFlush(notificationClient);
    }

    private void saveUserVerification(AppUser appUser, String token) throws Exception {
        // user verification token
        UserVerification userVerification = new UserVerification();
        userVerification.setCreatedBy(appUser.getId());
        userVerification.setToken(token);
        userVerification.setConsumed(false);
        userVerification.setStatus(Status.Active);
        // user Verification  save
        this.userVerificationRepository.saveAndFlush(userVerification);
    }

    private ResponseDTO validation(UserDTO userDTO) throws Exception {
        if (StringUtils.isEmpty(userDTO.getFirstName()) || StringUtils.isEmpty(userDTO.getLastName())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.FIRST_NAME_AND_LAST_NAME_REQUIRED);
        } else if (StringUtils.isEmpty(userDTO.getUsername()) || !BarcoUtil.isValidEmail(userDTO.getUsername())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.INVALID_EMAIL);
        } else if (StringUtils.isEmpty(userDTO.getPassword())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.PASSWORD_SHOULD_NOT_BE_EMPTY);
        } else if (BarcoUtil.isNull(userDTO.getRoles()) || userDTO.getRoles().size() == 0) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.INVALID_ROLE);
        } else if (StringUtils.isEmpty(userDTO.getTopicId()) || this.notificationClientRepository
                .findByTopicId(userDTO.getTopicId()).isPresent()) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.TOPIC_ID_EXIST);
        } else if (StringUtils.isEmpty(userDTO.getClientPath()) || this.notificationClientRepository
                .findByClientPath(userDTO.getClientPath()).isPresent()) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.CLIENT_PATH_EXIST);
        } else if (this.appUserRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.EMAIL_ALREADY_EXIST);
        } else if (BarcoUtil.isNull(userDTO.getAccessServices())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.ACCESS_SERVICE_MISSING);
        }
        return null;
    }

    private void saveUserRegistrationEmailDetail(UserDTO userDTO) throws Exception {
        Map<String, Object> emailDetail = new HashMap<>();
        emailDetail.put(EMAIL, userDTO.getUsername());
        emailDetail.put(FULLNAME, userDTO.getFirstName() + " " + userDTO.getLastName());
        emailDetail.put(TOKEN, userDTO.getToken());
        this.emailMessagesFactory.emailAccountCreated(emailDetail);
    }

    private void forgetPasswordEmailDetail(AppUser appUser, String token) throws Exception {
        Map<String, Object> emailDetail = new HashMap<>();
        emailDetail.put(EMAIL, appUser.getUsername());
        emailDetail.put(FULLNAME, appUser.getFirstName() + " " + appUser.getLastName());
        emailDetail.put(TOKEN, token);
        this.emailMessagesFactory.forgetPassword(emailDetail);
    }

    private String getToken() {
        return ApplicationConstants.BARCO_STRING +
                (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
    }

}
