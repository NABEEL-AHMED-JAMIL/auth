package com.barco.auth.service;

import com.barco.model.dto.request.NotificationRequest;
import com.barco.model.dto.response.NotificationResponse;
import com.barco.model.pojo.AppUser;

/**
 * @author Nabeel Ahmed
 */
public interface NotificationService {

    public void addNotification(NotificationRequest payload, AppUser appUser) throws Exception;

    public void sendNotificationToSpecificUser(NotificationResponse payload) throws Exception;

}
