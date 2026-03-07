package com.barco.auth.security;

import com.barco.common.cache.CacheService;
import com.barco.common.security.jwt.JwtSubject;
import com.barco.common.security.session.EtlAccountSessionDetail;
import com.barco.common.security.jwt.JwtFactory;
import com.barco.common.security.TokenBasedAuthentication;
import com.barco.common.utility.BarcoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Nabeel Ahmed
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private static final String BEARER = "Bearer ";

    private final CacheService cacheService;
    private final JwtFactory jwtFactory;
    private final EtlAccountDetailsService jwtAccountDetailsService;

    public TokenAuthenticationFilter(
        CacheService cacheService,
        JwtFactory jwtFactory,
        EtlAccountDetailsService jwtAccountDetailsService) {
        this.cacheService = cacheService;
        this.jwtFactory = jwtFactory;
        this.jwtAccountDetailsService = jwtAccountDetailsService;
    }

    /**
     * Method use to filter the request and set the authentication in the security context if the token is valid
     * @param request The HttpServletRequest object containing the client's request
     * @param response The HttpServletResponse object for sending a response to the client
     * @param filterChain The FilterChain object for invoking the next filter in the chain
     * @throws ServletException If an error occurs during filtering
     * @throws IOException If an I/O error occurs during filtering
     * */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            String authToken = this.parseJwt(request);
            if ((!BarcoUtil.isNull(authToken))) {
                // get the keyId from the token and then validate the token with the keyId and then extract the username from the token
                Map<String, Object> tokenHeader = this.jwtFactory.extractHeader(authToken);
                if (BarcoUtil.isNull(tokenHeader) || !tokenHeader.containsKey(BarcoUtil.KID_ID)) {
                    LOGGER.error("Invalid JWT token: missing keyId (kid) in header");
                    filterChain.doFilter(request, response);
                    return;
                }
                // extract the keyId (kid) from the token header
                String keyId = (String) tokenHeader.get(BarcoUtil.KID_ID);
                if (BarcoUtil.isNull(keyId)) {
                    LOGGER.error("Invalid JWT token: keyId (kid) is null in header");
                    filterChain.doFilter(request, response);
                    return;
                }
                // check if the public key for the keyId is present in the cache
                if (!this.cacheService.contains(keyId)) {
                    LOGGER.debug("JWT public key for keyId (kid) {} not found in cache, will attempt to load from database", keyId);
                    filterChain.doFilter(request, response);
                    return;
                }
                // validate the token with the public key from the cache
                byte[] publicKeyBytes = (byte[]) this.cacheService.get(keyId);
                if (!this.jwtFactory.verifyToken(authToken, publicKeyBytes)) {
                    LOGGER.error("Invalid JWT token: token validation failed for keyId (kid) {}", keyId);
                    filterChain.doFilter(request, response);
                    return;
                }
                // extract the subject from the token using the public key
                String subject = this.jwtFactory.getSubjectWithPublicKey(authToken, publicKeyBytes);
                if (BarcoUtil.isNull(subject)) {
                    LOGGER.error("Invalid JWT token: subject is null for keyId (kid) {}", keyId);
                    filterChain.doFilter(request, response);
                    return;
                }
                JwtSubject jwtSubject = this.jwtFactory.parseSubject(subject);
                if (BarcoUtil.isNull(jwtSubject) || BarcoUtil.isNull(jwtSubject.getUsername())) {
                    LOGGER.error("Invalid JWT token: could not parse subject or username is missing for kid {}", keyId);
                    filterChain.doFilter(request, response);
                    return;
                }
                String username = jwtSubject.getUsername();
                UserDetails principal = this.jwtAccountDetailsService.loadUserByUsername(username);
                LOGGER.debug("Authenticated user from JWT subject: {}", username);
                TokenBasedAuthentication tokenBasedAuthentication = new TokenBasedAuthentication(authToken, (EtlAccountSessionDetail) principal);
                tokenBasedAuthentication.setUserId(this.getHeader(request, BarcoUtil.X_USER_ID));
                tokenBasedAuthentication.setTenantId(this.getHeader(request, BarcoUtil.X_TENANT_ID));
                SecurityContextHolder.getContext().setAuthentication(tokenBasedAuthentication);
            }
        } catch (Exception ex) {
            LOGGER.error("Cannot set user authentication: {}", ex.getMessage(), ex);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Method use to parse the token from the request header
     * @param request The HttpServletRequest object containing the client's request
     * @return The JWT token extracted from the Authorization header, or null if the header is not present or does not start with "Bearer "
     * */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(BarcoUtil.Authorization);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER)) {
            return headerAuth.substring(7);
        }
        return null;
    }

    /**
     * Method use to get the header value from the request
     * @param request The HttpServletRequest object containing the client's request
     * @param headerName The name of the header to retrieve
     * @return The value of the specified header, or null if the header is not present or empty
     * */
    private String getHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        return StringUtils.hasText(headerValue) ? headerValue : null;
    }

}
