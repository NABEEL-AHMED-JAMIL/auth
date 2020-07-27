package com.barco.auth.service.Impl;


import com.barco.auth.repository.AppUserRepository;
import com.barco.auth.repository.AuthorityRepository;
import com.barco.auth.repository.UserVerificationRepository;
import com.barco.auth.service.AppUserService;
import com.barco.common.emailer.EmailMessagesFactory;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.TimeUtil;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.UserDTO;
import com.barco.model.enums.ApiCode;
import com.barco.model.enums.Status;
import com.barco.model.pojo.AppUser;
import com.barco.model.pojo.Authority;
import com.barco.model.pojo.NotificationClient;
import com.barco.model.pojo.UserVerification;
import com.barco.model.repository.NotificationClientRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;


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
    private NotificationClientRepository notificationClientRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Autowired
    private EmailMessagesFactory emailMessagesFactory;


    @Override
    public ResponseDTO saveUserRegistration(UserDTO userDTO) {
        if (StringUtils.isEmpty(userDTO.getFirstName()) || StringUtils.isEmpty(userDTO.getLastName())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.FIRST_NAME_AND_LAST_NAME_REQUIRED);
        } else if (StringUtils.isEmpty(userDTO.getUsername()) || !BarcoUtil.isValidEmail(userDTO.getUsername())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.INVALID_EMAIL);
        } else if (StringUtils.isEmpty(userDTO.getPassword())) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.PASSWORD_SHOULD_NOT_BE_EMPTY);
        } else if (StringUtils.isEmpty(userDTO.getRole()) || !this.authorityRepository.findByRole(userDTO.getRole()).isPresent()) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.INVALID_ROLE);
        } else if (StringUtils.isEmpty(userDTO.getTopicId()) || this.notificationClientRepository
                .findByTopicId(userDTO.getTopicId()).isPresent()) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.TOPIC_ID_EXIST);
        } else if (StringUtils.isEmpty(userDTO.getClientPath()) || this.notificationClientRepository
                .findByClientPath(userDTO.getClientPath()).isPresent()) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.CLIENT_PATH_EXIST);
        } else if (this.appUserRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            return new ResponseDTO(ApiCode.INVALID_REQUEST, ApplicationConstants.EMAIL_ALREADY_EXIST);
        }
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
        Optional<Authority> authority = this.authorityRepository.findByRole(userDTO.getRole());
        if (authority.isPresent()) {
            List<Authority> authorities = new ArrayList<>();
            authorities.add(authority.get());
            appUser.setAuthorities(authorities);
        }
        appUser.setUserType(userDTO.getUserType());
        appUser.setStatus(Status.Pending);
        // save user to db
        this.appUserRepository.save(appUser);
        this.appUserRepository.flush();
        // save the notification detail
        NotificationClient notificationClient = new NotificationClient();
        // client path
        if(StringUtils.isNotEmpty(userDTO.getClientPath())) {
            notificationClient.setClientPath(userDTO.getClientPath());
        }
        // topic id
        if(StringUtils.isNotEmpty(userDTO.getTopicId())) {
            notificationClient.setTopicId(userDTO.getTopicId());
        }
        notificationClient.setCreatedBy(appUser.getId());
        notificationClient.setModifiedBy(appUser.getModifiedBy());
        notificationClient.setAppUser(appUser);
        notificationClient.setStatus(Status.Pending);
        // save notification client
        this.notificationClientRepository.save(notificationClient);
        this.notificationClientRepository.flush();
        // user verification token
        String token = ApplicationConstants.BARCO_STRING +
                (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        UserVerification userVerification = new UserVerification();
        userVerification.setCreatedBy(appUser.getId());
        userVerification.setModifiedBy(appUser.getModifiedBy());
        userVerification.setAppUser(appUser);
        userVerification.setToken(token);
        userVerification.setConsumed(false);
        userVerification.setStatus(Status.Pending);
        // user Verification  save
        this.userVerificationRepository.save(userVerification);
        this.userVerificationRepository.flush();
        // user id set back for dto
        userDTO.setAppUserId(appUser.getId());
        userDTO.setToken(token);
        this.saveUserRegistrationEmailDetail(userDTO);
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, userDTO);
    }

    @Override
    public ResponseDTO emailTokenVerification(String token) {
        UserVerification userVerification = this.userVerificationRepository.findByToken(token);
        if (ObjectUtils.isEmpty(userVerification)) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.REQUEST_CANNOT_BE_PROCESSED);
        }
        AppUser appUser = userVerification.getAppUser();
        if (appUser.getStatus().value == Status.Active.value) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.ACCOUNT_ALREADY_ACTIVATED);
        }
        // notification detail save
        NotificationClient notificationClient = this.notificationClientRepository.findByAppUser(appUser);
        if (notificationClient != null) {
            notificationClient.setStatus(Status.Active);
            notificationClient.setModifiedBy(appUser.getId());
            notificationClient.setModifiedAt(new Timestamp(System.currentTimeMillis()));
            this.notificationClientRepository.saveAndFlush(notificationClient);
        }
        // verification detail activated time
        userVerification.setStatus(Status.Active);
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

    public ResponseDTO forgetPassword(String email) {
        AppUser appUser = this.appUserRepository.findByUsernameAndStatusNot(email.toLowerCase().trim(), Status.Delete);
        if (ObjectUtils.isEmpty(appUser)) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.USER_ID_NOT_EXIST);
        }
        String token = ApplicationConstants.BARCO_STRING +
                (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        UserVerification userVerification = new UserVerification();
        userVerification.setAppUser(appUser);
        userVerification.setExpiryDate(TimeUtil.addHoursInTimeStamp(new Timestamp(System.currentTimeMillis()), 24));
        userVerification.setCreatedBy(appUser.getId());
        userVerification.setStatus(Status.Pending);
        userVerification.setConsumed(false);
        userVerification.setToken(token);

        this.userVerificationRepository.save(userVerification);
        this.userVerificationRepository.flush();
        this.forgetPassword(appUser, token);
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.PASSWORD_RESET_EMAIL_MESSAGE);
    }

    @Override
    public ResponseDTO resetPassword(UserDTO userDTO) {
        UserVerification userVerification = this.userVerificationRepository.findByToken(userDTO.getToken());
        if (ObjectUtils.isEmpty(userVerification)) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.INVALID_REQUEST);
        } else {
            if (userVerification.getAppUser() != null && userVerification.getAppUser().getStatus().equals(Status.Delete)) {
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
        }
        AppUser appUser = userVerification.getAppUser();
        if (ObjectUtils.isEmpty(appUser)) {
            return new ResponseDTO(ApiCode.ERROR, ApplicationConstants.INVALID_REQUEST);
        }
        if(StringUtils.isNotBlank(userDTO.getPassword())) {
            appUser.setPassword(this.passwordEncoder.encode(userDTO.getPassword()));
        }
        userVerification.setStatus(Status.Active);
        userVerification.setModifiedBy(appUser.getId());
        userVerification.setPasswordAdded(true);
        userVerification.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        userVerification.setActivatedAt(new Timestamp(System.currentTimeMillis()));
        userVerification.setConsumed(true);
        // app user repo
        this.appUserRepository.save(appUser);
        this.appUserRepository.flush();
        // user verfication repo
        this.userVerificationRepository.save(userVerification);
        this.userVerificationRepository.flush();
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.PASSWORD_RESET_SUCCESS);
    }


    private void saveUserRegistrationEmailDetail(UserDTO userDTO) {
        Map<String, Object> emailDetail = new HashMap<>();
        emailDetail.put(EMAIL, userDTO.getUsername());
        emailDetail.put(FULLNAME, userDTO.getFirstName() + " " + userDTO.getLastName());
        emailDetail.put(TOKEN, userDTO.getToken());
        this.emailMessagesFactory.emailAccountCreated(emailDetail);
    }

    private void forgetPassword(AppUser appUser, String token) {
        Map<String, Object> emailDetail = new HashMap<>();
        emailDetail.put(EMAIL, appUser.getUsername());
        emailDetail.put(FULLNAME, appUser.getFirstName() + " " + appUser.getLastName());
        emailDetail.put(TOKEN, token);
        this.emailMessagesFactory.forgetPassword(emailDetail);
    }

}
