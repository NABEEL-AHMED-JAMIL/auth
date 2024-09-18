package com.barco.auth.config;

import com.barco.auth.filter.AuthTokenFilter;
import com.barco.common.security.AuthEntryPointJwt;
import com.barco.model.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public WebSecurityConfig() {}

    /**
     * Method use to add authentication jwt
     * @return AuthTokenFilter
     * */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Method use to add authentication manger builder
     * @param authenticationManagerBuilder
     * */
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(passwordEncoder());
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
        http.cors().and().csrf().disable().exceptionHandling().authenticationEntryPoint(this.unauthorizedHandler).and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
        .antMatchers("/auth.json/**", "/ws/**", "/user/**").permitAll()
        .antMatchers("/api/v2/**").permitAll().anyRequest().authenticated();
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * method use to configure teh white list url
     * @param web
     * */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
            "/configuration/security", "/swagger-ui.html", "/webjars/**");
    }

}
