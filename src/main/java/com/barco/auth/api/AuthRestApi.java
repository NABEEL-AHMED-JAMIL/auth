package com.barco.auth.api;

import com.barco.auth.domain.dto.UserTokenState;
import com.barco.auth.security.JwtAuthenticationRequest;
import com.barco.auth.security.TokenHelper;
import com.barco.common.util.ExceptionUtil;
import com.barco.model.ApplicationDecorator;
import com.barco.model.enums.Status;
import com.barco.model.pojo.NotificationClient;
import com.barco.model.pojo.User;
import com.barco.model.service.NotificationClientService;
import com.barco.model.util.ModelUtil;
import com.barco.model.util.ReturnConstants;
import io.swagger.annotations.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author Nabeel.amd
 */
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = { "Barco-Auth := Barco-Auth EndPoint" })
public class AuthRestApi {

    public Logger logger = LogManager.getLogger(AuthRestApi.class);

    private String json = "{ \"status\": \"%s\" }";

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @Autowired
    private ModelUtil modelUtil;

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private ReturnConstants returnConstants;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private NotificationClientService notificationClientService;


    public AuthRestApi() { }

    // method use for login
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody String jsonRequest) {
        long startTime = System.currentTimeMillis();
        ApplicationDecorator decorator = new ApplicationDecorator();
        try {
            JwtAuthenticationRequest authRequest = (JwtAuthenticationRequest)
                  this.modelUtil.populateDataBeanFromJSON(JwtAuthenticationRequest.class, decorator, jsonRequest);
            if(decorator.getResponseMessage() == null) {
                final Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                User login_user = (User)authentication.getPrincipal();
                logger.info("User Found In Db With Id {}.", login_user.getId());
                String jws_token = this.tokenHelper.generateToken(login_user.getUsername());
                UserTokenState user_token_state = new UserTokenState();
                UserTokenState.User token_user_info = new UserTokenState.User();
                if(login_user.getId() != null) { token_user_info.setMemberId(login_user.getId()); }
                if(login_user.getImageUrl() != null) { token_user_info.setImageUrl(login_user.getImageUrl()); }
                if(login_user.getFirstName() != null) { token_user_info.setFirstName(login_user.getFirstName()); }
                if(login_user.getLastName() != null) { token_user_info.setLastName(login_user.getLastName()); }
                user_token_state.setAccessUser(token_user_info);
                user_token_state.setAccess_token(jws_token);
                user_token_state.setExpires_time(Long.valueOf(EXPIRES_IN));
                Optional<NotificationClient> notificationClientPresent = this.notificationClientService.findByMemberId(login_user, Status.Active);
                if(notificationClientPresent.isPresent()) {
                    NotificationClient notification_client = notificationClientPresent.get();
                    UserTokenState.Notification token_user_notification = new UserTokenState.Notification();
                    if(notification_client.getTopicId() != null) { token_user_notification.setTopicId(notification_client.getTopicId()); }
                    if(notification_client.getClientPath() != null) { token_user_notification.setClientPath(notification_client.getClientPath()); }
                    user_token_state.setNotification(token_user_notification);
                }
                decorator.setDataBean(user_token_state);
            }
        } catch (BadCredentialsException ex) {
            decorator.setResponseMessage(this.returnConstants.WRONG_CREDENTIALS);
            decorator.setReturnCode(this.returnConstants.ReturnCodeFailure);
            decorator.getErrors().add(ExceptionUtil.getRootCauseMessage(ex));
        } catch (Exception ex) {
            decorator.setResponseMessage(this.returnConstants.TECHNICAL_ISSUE);
            decorator.setReturnCode(this.returnConstants.ReturnCodeFailure);
            decorator.getErrors().add(ExceptionUtil.getRootCauseMessage(ex));
        }
        logger.debug("createAuthenticationToken : " + jsonRequest);
        decorator.setApiName("/auth/login");
        decorator.setQueryTime(String.valueOf(System.currentTimeMillis() - startTime));
        logger.info("Api-Response Time :- " + decorator.getQueryTime());
        return ResponseEntity.ok(this.modelUtil.responseToClient(decorator));
    }

    // method use to check the toke valid or not and also help for mic-service
    @RequestMapping(value = "/isValid/token", method = RequestMethod.GET)
    public ResponseEntity<?> isValidToken() {
        // Return the token status
        return ResponseEntity.ok(String.format(json, "Pass"));
    }

}