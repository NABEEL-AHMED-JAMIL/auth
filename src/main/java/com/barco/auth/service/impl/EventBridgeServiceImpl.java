package com.barco.auth.service.impl;

import com.barco.auth.service.EventBridgeService;
import com.barco.common.security.JwtUtils;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.dto.request.*;
import com.barco.model.dto.response.*;
import com.barco.model.pojo.AppUser;
import com.barco.model.pojo.AppUserEventBridge;
import com.barco.model.pojo.Credential;
import com.barco.model.pojo.EventBridge;
import com.barco.model.repository.AppUserEventBridgeRepository;
import com.barco.model.repository.AppUserRepository;
import com.barco.model.repository.EventBridgeRepository;
import com.barco.model.util.MessageUtil;
import com.barco.model.util.lookup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @author Nabeel Ahmed
 */
@Service
public class EventBridgeServiceImpl implements EventBridgeService {

    private Logger logger = LoggerFactory.getLogger(EventBridgeServiceImpl.class);

    @Value("${storage.efsFileDire}")
    private String tempStoreDirectory;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private QueryService queryService;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private EventBridgeRepository eventBridgeRepository;
    @Autowired
    private AppUserEventBridgeRepository appUserEventBridgeRepository;

    public EventBridgeServiceImpl() {}

    /**
     * Method to link the EventBridge with user
     * @param payload
     * @return AppResponse
     * @throws Exception
     */
    @Override
    public AppResponse linkEventBridgeWithUser(LinkEBURequest payload) throws Exception {
        logger.info("Request linkEventBridgeWithUser :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        }
        validationResponse = this.validateLinkEventBridgePayload(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        }
        Optional<AppUser> superAdmin = this.getAppUser(payload.getSessionUser().getUsername());
        Optional<EventBridge> eventBridge = this.eventBridgeRepository.findById(payload.getId());
        if (eventBridge.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.EVENT_BRIDGE_NOT_FOUND_WITH_ID, payload.getId()), payload);
        } else if (BarcoUtil.isNull(eventBridge.get().getCredential())) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.EVENT_BRIDGE_NOT_FOUND_LINK_CREDENTIAL_WITH_ID, payload.getId()), payload);
        }
        Optional<AppUser> appUser = this.appUserRepository.findById(payload.getAppUserId());
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.APPUSER_NOT_FOUND, payload.getAppUserId()), payload);
        } else if (payload.getLinked()) {
            return linkEventBridge(superAdmin.get(), appUser.get(), eventBridge.get(), payload);
        } else {
            this.queryService.deleteQuery(String.format(QueryService.DELETE_APP_USER_EVENT_BRIDGE_BY_EVENT_BRIDGE_ID_AND_APP_USER_ID, eventBridge.get().getId(), appUser.get().getId()));
        }
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, ""), payload);
    }

    /**
     * Method use to get the appuser by username
     * @param username
     * @return AppUser
     * */
    private Optional<AppUser> getAppUser(String username) {
        return this.appUserRepository.findByUsernameAndStatus(username, APPLICATION_STATUS.ACTIVE);
    }

    /**
     * Method use to link with event bridge
     * @param superAdmin
     * @param appUser
     * @param eventBridge
     * @param payload
     * @return AppResponse
     *
     * */
    private AppResponse linkEventBridge(AppUser superAdmin, AppUser appUser,
        EventBridge eventBridge, LinkEBURequest payload) throws Exception {
        Credential credential = eventBridge.getCredential();
        if (BarcoUtil.isNull(credential.getContent())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.CREDENTIAL_CONTENT_MISSING);
        }
        String priKey = this.getCredentialPrivateKey(credential);
        AppUserEventBridge linkEventBridge = this.getAppUserEventBridge(superAdmin, appUser, eventBridge);
        linkEventBridge.setAccessToken(this.jwtUtils.generateToken(priKey, linkEventBridge.getTokenId()));
        linkEventBridge.setExpireTime(this.getOneYearFromNow());
        this.appUserEventBridgeRepository.save(linkEventBridge);
        payload.setAccessToken(linkEventBridge.getAccessToken());
        payload.setExpireTime(linkEventBridge.getExpireTime());
        payload.setTokenId(linkEventBridge.getTokenId());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, ""), payload);
    }

    /**
     * Method use to validate the username
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    private AppResponse validateUsername(Object payload) {
        SessionUser sessionUser = null;
        // Check if the payload is an instance of RoleRequest or other types
        if (payload instanceof EventBridgeRequest) {
            EventBridgeRequest eventBridgeRequest = (EventBridgeRequest) payload;
            sessionUser = eventBridgeRequest.getSessionUser();
        } else if (payload instanceof LinkEBURequest) {
            LinkEBURequest linkEBURequest = (LinkEBURequest) payload;
            sessionUser = linkEBURequest.getSessionUser();
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
