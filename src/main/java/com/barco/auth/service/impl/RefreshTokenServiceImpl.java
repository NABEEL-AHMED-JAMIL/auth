package com.barco.auth.service.impl;

import com.barco.auth.service.RefreshTokenService;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.dto.request.TokenRefreshRequest;
import com.barco.model.dto.response.AppResponse;
import com.barco.model.dto.response.QueryResponse;
import com.barco.model.pojo.AppUser;
import com.barco.model.pojo.RefreshToken;
import com.barco.model.repository.AppTokenRepository;
import com.barco.model.repository.AppUserRepository;
import com.barco.model.util.MessageUtil;
import com.barco.model.util.lookup.APPLICATION_STATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Nabeel Ahmed
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private Logger logger = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private QueryService queryService;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private AppTokenRepository appTokenRepository;

    public RefreshTokenServiceImpl() {}

    /**
     * Method use to fetch refresh token statistics
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchSessionStatistics() throws Exception {
        logger.info("Request fetchSessionStatistics");
        QueryResponse queryResponse = this.queryService.executeQueryResponse(QueryService.SESSION_STATISTICS);
        queryResponse.setQuery((String) BarcoUtil.NULL);
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, queryResponse);
    }

    /**
     * Method use to fetch all refresh token
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchByAllRefreshToken(TokenRefreshRequest payload) throws Exception {
        logger.info("Request fetchByAllRefreshToken{}.", payload);
        if (BarcoUtil.isNull(payload.getStartDate())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.START_DATE_MISSING);
        } else if (BarcoUtil.isNull(payload.getEndDate())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.END_DATE_MISSING);
        }
        Timestamp startDate = Timestamp.valueOf(payload.getStartDate().concat(BarcoUtil.START_DATE));
        Timestamp endDate = Timestamp.valueOf(payload.getEndDate().concat(BarcoUtil.END_DATE));
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY,
            this.appTokenRepository.findByDateCreatedBetweenOrderByDateCreatedDesc(startDate, endDate)
                .stream().map(this::getRefreshTokenResponse).collect(Collectors.toList()));
    }

    /**
     * findByToken use for get the refresh token from db
     * @param token
     * @return Optional<RefreshToken>
     * @throws Exception
     * */
    public Optional<RefreshToken> findByToken(String token) throws Exception {
        logger.info("Request findByToken :- {}.", token);
        return this.appTokenRepository.findByTokenAndStatus(token, APPLICATION_STATUS.ACTIVE);
    }

    /**
     * createRefreshToken use to create refresh token into db
     * @param appUserId
     * @param ip
     * @return RefreshToken
     * @throws Exception
     * */
    public RefreshToken createRefreshToken(Long appUserId, String ip) throws Exception {
        logger.info("Request createRefreshToken :- AppUser Id :- {} & IP :- {}.", appUserId, ip);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setIpAddress(ip);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setStatus(APPLICATION_STATUS.ACTIVE);
        AppUser appUser = this.appUserRepository.findById(appUserId).orElseThrow(() -> new NullPointerException(MessageUtil.APPUSER_NOT_FOUND));
        refreshToken.setExpiryDate(Instant.now().plusMillis(this.refreshTokenDurationMs));
        refreshToken.setCreatedBy(appUser);
        refreshToken.setUpdatedBy(appUser);
        return this.appTokenRepository.save(refreshToken);
    }

    /**
     * verifyExpiration use to create refresh token into db
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    public AppResponse verifyExpiration(RefreshToken payload) throws Exception {
        if (payload.getExpiryDate().compareTo(Instant.now()) < 0) {
            payload.setStatus(APPLICATION_STATUS.DELETE);
            this.appTokenRepository.save(payload);
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.REFRESH_TOKEN_EXPIRED);
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.REFRESH_TOKEN_VALID, payload);
    }

    /**
     * deleteRefreshToken use to delete refresh token from db
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    public AppResponse deleteRefreshToken(TokenRefreshRequest payload) throws Exception {
        logger.info("Request deleteRefreshToken :- {}.", payload);
        Optional<RefreshToken> refreshToken = this.findByToken(payload.getRefreshToken());
        if (refreshToken.isPresent()) {
            refreshToken.get().setStatus(APPLICATION_STATUS.DELETE);
            this.appTokenRepository.save(refreshToken.get());
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.REFRESH_TOKEN_DELETED, payload);
    }

    /**
     * deleteAllRefreshToken use to delete all refresh token from db
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteAllRefreshToken(TokenRefreshRequest payload) throws Exception {
        logger.info("Request deleteAllRefreshToken :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuids())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.REFRESH_TOKEN_IDS_MISSING);
        }
        this.appTokenRepository.findAllByUuidInAndStatusNot(payload.getUuids(), APPLICATION_STATUS.DELETE)
        .forEach(refreshToken -> {
            refreshToken.setStatus(APPLICATION_STATUS.DELETE);
            this.appTokenRepository.save(refreshToken);
        });
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.REFRESH_TOKEN_DELETED, payload);
    }
}