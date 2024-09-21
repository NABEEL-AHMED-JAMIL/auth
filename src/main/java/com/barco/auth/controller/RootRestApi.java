package com.barco.auth.controller;

import com.barco.model.dto.request.SessionUser;
import com.barco.model.security.UserSessionDetail;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

/**
 * Api use to perform crud operation
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins="*")
public class RootRestApi {

    /**
     * Method use to reutrn the session ser
     * @return SessionUser
     * **/
    public SessionUser getSessionUser() {
        UserSessionDetail userSessionDetail = (UserSessionDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new SessionUser(userSessionDetail.getUuid(), userSessionDetail.getEmail(), userSessionDetail.getUsername());
    }
}
