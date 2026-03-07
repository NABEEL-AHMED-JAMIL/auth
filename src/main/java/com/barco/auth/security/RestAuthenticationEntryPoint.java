package com.barco.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Nabeel Ahmed
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private Logger LOGGER = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    /**
     * Method use to handle unauthorized error
     * @param request
     * @param response
     * @param authException
     * @throws IOException
     * */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        LOGGER.error("Unauthorized error: {}", authException.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }

}