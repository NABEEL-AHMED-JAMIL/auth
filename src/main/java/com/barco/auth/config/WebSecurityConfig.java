package com.barco.auth.config;

import com.barco.auth.security.EtlAccountDetailsService;
import com.barco.auth.security.RestAuthenticationEntryPoint;
import com.barco.auth.security.TokenAuthenticationFilter;
import com.barco.common.cache.CacheService;
import com.barco.common.security.jwt.JwtFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Nabeel Ahmed
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

    // Pretty, centralized whitelist for actuator + swagger + open-api
    private static final String[] PUBLIC_SWAGGER_ACTUATOR = new String[] {
        "/actuator/**",
        "/openapi.yml",
        "/v2/api-docs",
        "/v2/api-docs/**",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/auth/**"
    };

    @Qualifier("jwtPublicKeyCache")
    private final CacheService cacheService;
    private final JwtFactory jwtFactory;
    private final EtlAccountDetailsService jwtAccountDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public WebSecurityConfig(
        CacheService cacheService,
        JwtFactory jwtFactory,
        EtlAccountDetailsService jwtAccountDetailsService,
        RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.cacheService = cacheService;
        this.jwtFactory = jwtFactory;
        this.jwtAccountDetailsService = jwtAccountDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        LOGGER.info("WebSecurityConfig initialized");
    }

    /**
     * Method use to add authentication jwt
     * @return AuthTokenFilter
     * */
    @Bean
    public TokenAuthenticationFilter authenticationJwtTokenFilter() {
        LOGGER.debug("Creating TokenAuthenticationFilter bean");
        return new TokenAuthenticationFilter(this.cacheService, this.jwtFactory, this.jwtAccountDetailsService);
    }

    /**
     * Method use to add authentication manger builder
     * @param authenticationManagerBuilder
     * */
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        LOGGER.debug("Configuring AuthenticationManagerBuilder with EtlAccountDetailsService and password encoder");
        authenticationManagerBuilder.userDetailsService(this.jwtAccountDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * Method use to add authentication manger
     * @return AuthenticationManager
     * */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * method use to encode the password
     * @return PasswordEncoder
     * */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * method ue to configure the http
     * @param http
     * */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        LOGGER.info("Configuring HttpSecurity - minimal public whitelist: actuator, swagger, openapi.yml");
        http.cors()
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().csrf().disable().exceptionHandling().authenticationEntryPoint(this.restAuthenticationEntryPoint)
            .and().authorizeRequests()
                .antMatchers(PUBLIC_SWAGGER_ACTUATOR).permitAll()
                .anyRequest().authenticated();
        // Add token filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * method use to configure the white list url
     * @param web
     * */
    @Override
    public void configure(WebSecurity web) throws Exception {
        LOGGER.info("Configuring WebSecurity to ignore actuator, swagger and openapi.yml");
        web.ignoring().antMatchers(PUBLIC_SWAGGER_ACTUATOR);
    }

}
