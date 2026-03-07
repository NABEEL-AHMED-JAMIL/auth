package com.barco.auth.service.impl;

import com.barco.auth.payload.request.*;
import com.barco.auth.payload.response.AuthResponse;
import com.barco.auth.service.EtlAuthService;
import com.barco.common.cache.CacheService;
import com.barco.common.payload.APIResponse;
import com.barco.common.security.jwt.JwtFactory;
import com.barco.common.security.jwt.JwtSubject;
import com.barco.common.security.session.EtlAccountSessionDetail;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.lookup.APPLICATION_STATUS;
import com.barco.model.repository.OrganizationJwtKeyRepository;
import com.barco.model.repository.projection.OrganizationKeyProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * @author Nabeel Ahmed
 */
@Service
public class EtlAuthServiceImpl implements EtlAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtlAuthServiceImpl.class);

    @Value("${etl.jwt.secret}")
    private String jwtSecret;
    @Value("${etl.jwt.jwtExpirationMs}")
    private Long jwtExpirationMs;
    @Value("${etl.jwt.jwtRefreshExpirationMs}")
    private Long jwtRefreshExpirationMs;

    @Qualifier("jwtPublicKeyCache")
    private final CacheService cacheService;
    private final JwtFactory jwtFactory;
    private final OrganizationJwtKeyRepository organizationJwtKeyRepository;
    private final AuthenticationManager authenticationManager;

    public EtlAuthServiceImpl(
        CacheService cacheService,
        JwtFactory jwtFactory,
        OrganizationJwtKeyRepository organizationJwtKeyRepository,
        AuthenticationManager authenticationManager) {
        this.cacheService = cacheService;
        this.jwtFactory = jwtFactory;
        this.organizationJwtKeyRepository = organizationJwtKeyRepository;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Authenticate user credentials and generate JWT tokens upon successful authentication.
     * Caches the organization's active JWT public key if not already cached.
     * @param payload login request containing username and password
     * @return APIResponse with AuthResponse on success or error message on failure
     */
    @Override
    public APIResponse<?> signIn(LoginRequest payload) {
        LOGGER.info("Attempting sign-in for username={}", payload.getUsername());
        try {
            final Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            EtlAccountSessionDetail etlAccountSessionDetail = (EtlAccountSessionDetail) authentication.getPrincipal();

            // Fetch the active JWT key for the user's organization using projection to avoid LOB mapping issues
            Optional<OrganizationKeyProjection> organizationKeyProjection = this.organizationJwtKeyRepository.findProjectedByOrganizationIdAndStatus(
                etlAccountSessionDetail.getOrganization().getId(), APPLICATION_STATUS.ACTIVE);
            if (organizationKeyProjection.isEmpty()) {
                LOGGER.warn("No active JWT key found for organization ID={}", etlAccountSessionDetail.getOrganization().getId());
                return APIResponse.unauthorized("Authentication failed.");
            }

            // Cache the public key if not already cached
            OrganizationKeyProjection organizationKeyProjection1 = organizationKeyProjection.get();
            if (!this.cacheService.contains(organizationKeyProjection1.getKeyId())) {
                LOGGER.info("Caching public key for organization ID={}, keyId={}", etlAccountSessionDetail.getOrganization().getId(), organizationKeyProjection1.getKeyId());
                this.cacheService.put(organizationKeyProjection1.getKeyId(), organizationKeyProjection1.getPublicKey());
            }
            AuthResponse authResponse = this.getAuthResponse(etlAccountSessionDetail, organizationKeyProjection1);
            LOGGER.info("User {} signed in successfully, userId={}", etlAccountSessionDetail.getUsername(), etlAccountSessionDetail.getUuid());
            return APIResponse.success("Signed in successfully", authResponse);
        } catch (AuthenticationException ae) {
            LOGGER.warn("Authentication failed for username={}: {}", payload.getUsername(), ae.getMessage());
            return APIResponse.unauthorized("Invalid username or password");
        } catch (IllegalArgumentException iae) {
            LOGGER.error("Invalid argument during sign-in for username={}", payload.getUsername(), iae);
            return APIResponse.error("Invalid sign-in request: " + iae.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during sign-in for username={}", payload.getUsername(), e);
            return APIResponse.error("Error while signing in. Please try again later.");
        }
    }

    /**
     * Placeholder for user sign-up method. Implementation can be added as needed.
     * Currently returns null to indicate not implemented.
     * @param payload sign-up request containing user registration details
     * @return APIResponse indicating success or failure of the sign-up process
     */
    @Override
    public APIResponse<?> signup(SignupRequest payload) throws Exception {
        return null;
    }

    /**
     * Placeholder for forgot password method. Implementation can be added as needed.
     * Currently returns null to indicate not implemented.
     * @param payload forgot password request containing user email or username
     * @return APIResponse indicating success or failure of the forgot password process
     */
    @Override
    public APIResponse<?> forgotPassword(ForgotPasswordRequest payload) throws Exception {
        return null;
    }

    /**
     * Placeholder for password reset method. Implementation can be added as needed.
     * Currently returns null to indicate not implemented.
     * @param payload password reset request containing new password and reset token
     * @return APIResponse indicating success or failure of the password reset process
     */
    @Override
    public APIResponse<?> resetPassword(PasswordResetRequest payload) throws Exception {
        return null;
    }

    /**
     * Placeholder for token refresh method. Implementation can be added as needed.
     * Currently returns null to indicate not implemented.
     * @param payload token refresh request containing the refresh token
     * @return APIResponse with new access token on success or error message on failure
     */
    @Override
    public APIResponse<?> refreshToken(TokenRefreshRequest payload) throws Exception {
        return null;
    }

    /**
     * Placeholder for email verification method. Implementation can be added as needed.
     * Currently returns null to indicate not implemented.
     * @param payload verify email request containing verification token
     * @return APIResponse indicating success or failure of the email verification process
     */
    @Override
    public APIResponse<?> verifyEmail(VerifyEmailRequest payload) throws Exception {
        return null;
    }

    /**
     * Placeholder for resend verification email method. Implementation can be added as needed.
     * Currently returns null to indicate not implemented.
     * @param payload resend verification request containing user email or username
     * @return APIResponse indicating success or failure of the resend verification process
     */
    @Override
    public APIResponse<?> resendVerification(ResendVerificationRequest payload) throws Exception {
        return null;
    }

    /**
     * Generate JWT access and refresh tokens for the authenticated user session using the organization's active JWT key.
     * @param sessionDetail authenticated user session details
     * @param keyProj organization JWT key projection containing key ID and private key bytes
     * @return AuthResponse containing tokens and user info
     */
    private AuthResponse getAuthResponse(
        EtlAccountSessionDetail sessionDetail,
        OrganizationKeyProjection keyProj) throws Exception {
        // Create JWT subject with user details
        JwtSubject jwtSubject = new JwtSubject()
            .setUuid(sessionDetail.getUuid())
            .setFirstName(sessionDetail.getFirstName())
            .setLastName(sessionDetail.getLastName())
            .setUsername(sessionDetail.getUsername())
            .setEmail(sessionDetail.getEmail());

        // Handle private key bytes: if they look like Base64 text, use as-is; otherwise, Base64-encode the raw bytes
        byte[] privateKeyBytes = keyProj.getPrivateKey();
        if (BarcoUtil.isNull(privateKeyBytes) || privateKeyBytes.length == 0) {
            LOGGER.error("Organization private key is missing for orgId={}", sessionDetail.getOrganization().getId());
            throw new IllegalStateException("Organization private key not available");
        }

        // Generate access and refresh tokens using the organization's active JWT key
        String accessToken = this.jwtFactory.generateToken(privateKeyBytes,
                this.jwtSecret, keyProj.getKeyId(), jwtSubject.toString(), this.jwtExpirationMs);
        String refreshToken = this.jwtFactory.generateRefreshToken(privateKeyBytes,
                this.jwtSecret, keyProj.getKeyId(), jwtSubject.toString(), this.jwtRefreshExpirationMs);

        // Build the authentication response with tokens and user details
        return new AuthResponse()
            .setAccessToken(accessToken)
            .setRefreshToken(refreshToken)
            .setTokenType("Bearer")
            .setExpiresIn(this.jwtExpirationMs)
            .setIssuedAt(System.currentTimeMillis())
            .setUserId(sessionDetail.getUuid())
            .setUsername(sessionDetail.getUsername())
            .setActiveProfile(sessionDetail.getAccountProfile());
    }

}
