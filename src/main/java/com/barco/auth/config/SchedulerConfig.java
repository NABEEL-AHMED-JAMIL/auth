package com.barco.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enable scheduling for the auth module.
 * This allows us to define @Scheduled methods in this module, such as for cache warmup or cleanup tasks.
 * @author Nabeel Ahmed
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfig.class);

    public SchedulerConfig() {
        LOGGER.info("SchedulerConfig initialized - scheduling enabled");
    }

}
