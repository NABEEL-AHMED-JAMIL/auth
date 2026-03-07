package com.barco.auth.scheduler;

import com.barco.common.cache.CacheService;
import com.barco.common.cache.JwtPublicKeyCache;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.lookup.APPLICATION_STATUS;
import com.barco.model.repository.OrganizationJwtKeyRepository;
import com.barco.model.repository.projection.OrganizationKeyProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Scheduler to periodically warm up the JWT public key cache by fetching active keys from the database.
 * This helps ensure that the cache is populated and reduces latency for JWT validation.
 * @author Nabeel Ahmed
 */
@Component
public class JwtKeyCacheScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtKeyCacheScheduler.class);

    @Qualifier("jwtPublicKeyCache")
    private final CacheService cacheService;
    private final OrganizationJwtKeyRepository organizationJwtKeyRepository;

    public JwtKeyCacheScheduler(
        CacheService cacheService,
        OrganizationJwtKeyRepository organizationJwtKeyRepository) {
        this.cacheService = cacheService;
        this.organizationJwtKeyRepository = organizationJwtKeyRepository;
        LOGGER.info("JwtKeyCacheWarmupScheduler initialized");
    }

    /**
     * Scheduled method to warm up the JWT public key cache.
     * Runs at a fixed interval defined by application properties, with an initial delay.
     * Fetches active organization keys and populates the cache.
     */
    @Scheduled(initialDelayString = "${auth.cache.warmup.initial-delay-ms:5000}",
        fixedDelayString = "${auth.cache.warmup.interval-ms:43200000}")
    public void orgJwtKeyCacheScheduler() {
        try {
            List<OrganizationKeyProjection> organizationKeys =
                this.organizationJwtKeyRepository.findAllProjectedByStatus(APPLICATION_STATUS.ACTIVE);
            if (BarcoUtil.isNull(organizationKeys)) {
                LOGGER.debug("No active organization JWT keys found for cache warmup");
                return;
            }
            organizationKeys.parallelStream()
            .forEach(proj -> {
                if (!BarcoUtil.isNull(proj.getKeyId()) && !BarcoUtil.isNull(proj.getPublicKey())) {
                    LOGGER.info("Warming up JWT public key cache for keyId={}", proj.getKeyId());
                    this.cacheService.put(proj.getKeyId(), proj.getPublicKey());
                };
            });
        } catch (Exception ex) {
            LOGGER.error("Error during JWT key cache warmup", ex);
        }
    }
}
