package com.barco.auth.service.impl;

import com.barco.auth.service.LookupDataCacheService;
import com.barco.auth.service.NotificationService;
import com.barco.model.dto.request.NotificationRequest;
import com.barco.model.dto.response.MessageResponse;
import com.barco.model.dto.response.NotificationResponse;
import com.barco.model.pojo.AppUser;
import com.barco.model.pojo.NotificationAudit;
import com.barco.model.repository.AppUserRepository;
import com.barco.model.repository.NotificationAuditRepository;
import com.barco.model.util.lookup.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Nabeel Ahmed
 * TemplateReg can be email and etc
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final String REPLAY = "/reply";
    private final String NOTIFY_ID = "id";

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private LookupDataCacheService lookupDataCacheService;
    @Autowired
    private NotificationAuditRepository notificationAuditRepository;

    public NotificationServiceImpl() {}

    /**
     * Method use send notification to user session
     * @param payload
     * @param appUser
     * @throws Exception
     * */
    @Override
    public void addNotification(NotificationRequest payload, AppUser appUser) throws Exception {
        logger.info("Request addNotification :- {}.", payload);
        NotificationAudit notificationAudit = new NotificationAudit();
        notificationAudit.setSendTo(appUser);
        notificationAudit.setMessage(payload.getBody().toString());
        notificationAudit.setNotifyType(NOTIFICATION_TYPE.getByLookupCode(payload.getNotifyType()));
        notificationAudit.setMessageStatus(NOTIFICATION_STATUS.getByLookupCode(payload.getMessageStatus()));
        notificationAudit.setExpireTime(payload.getExpireTime());
        notificationAudit.setCreatedBy(appUser);
        notificationAudit.setUpdatedBy(appUser);
        notificationAudit.setStatus(APPLICATION_STATUS.ACTIVE);
        this.notificationAuditRepository.save(notificationAudit);
        payload.setId(notificationAudit.getId());
        payload.setDateCreated(notificationAudit.getDateCreated());
        this.sendNotificationToSpecificUser(this.getNotificationResponse(notificationAudit));
    }

    /**
     * sendNotificationToSpecificUser method use to send the notification to specific user
     * @param payload
     * @throws Exception
     * */
    @Override
    public void sendNotificationToSpecificUser(NotificationResponse payload) throws Exception {
        logger.info("Request sendNotificationToSpecificUser :- {}.", payload);
        this.simpMessagingTemplate.convertAndSendToUser(payload.getSendTo(), REPLAY, payload);
    }

    /**
     * Method use to add notification response
     * @param payload
     * @return NotificationResponse
     * */
    private NotificationResponse getNotificationResponse(NotificationAudit payload) {
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setUuid(payload.getUuid());
        notificationResponse.setSendTo(payload.getSendTo().getUsername());
        notificationResponse.setBody(new Gson().fromJson(payload.getMessage(), MessageResponse.class));
        notificationResponse.setNotifyType(GLookup.getGLookup(this.lookupDataCacheService.getChildLookupDataByParentLookupTypeAndChildLookupCode(
            NOTIFICATION_TYPE.getName(), payload.getNotifyType().getLookupCode())));
        notificationResponse.setMessageStatus(GLookup.getGLookup(this.lookupDataCacheService.getChildLookupDataByParentLookupTypeAndChildLookupCode(
            NOTIFICATION_STATUS.getName(), payload.getMessageStatus().getLookupCode())));
        notificationResponse.setExpireTime(payload.getExpireTime());
        notificationResponse.setDateCreated(payload.getDateCreated());
        notificationResponse.setDateUpdated(payload.getDateCreated());
        return notificationResponse;
    }
}
