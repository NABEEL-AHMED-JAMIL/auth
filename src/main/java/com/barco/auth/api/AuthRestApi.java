package com.barco.auth.api;

import com.barco.auth.service.AuthTokenService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.common.utility.ExceptionUtil;
import com.barco.model.dto.JwtAuthenticationRequest;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.enums.ApiCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/auth.json", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = { "Barco-Auth := Barco-Auth EndPoint" })
public class AuthRestApi {

    public Logger logger = LogManager.getLogger(AuthRestApi.class);

    @Autowired
    private AuthTokenService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "User login", notes = "Endpoint help to login user.")
    public ResponseDTO login(@RequestBody JwtAuthenticationRequest authenticationReq) {
        ResponseDTO response = null;
        try {
            final Authentication authentication = this.authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authenticationReq.getUsername()
                .toLowerCase().trim(), authenticationReq.getPassword()));
            // Inject into security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            response = this.authService.login(authenticationReq);
            logger.info("Request for login " + authenticationReq);
          } catch (Exception ex) {
            logger.info("Error during login " + ExceptionUtil.getRootCause(ex));
            response = new ResponseDTO(ApiCode.ERROR, ApplicationConstants.INVALID_CREDENTIAL_MSG);
        }
        return response;
    }

}
