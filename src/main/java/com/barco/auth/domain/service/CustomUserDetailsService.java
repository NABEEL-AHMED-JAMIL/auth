package com.barco.auth.domain.service;

import com.barco.auth.domain.dto.UserRequest;
import com.barco.common.manager.aws.dto.AwsBucketObjectDetail;
import com.barco.common.manager.aws.impl.AwsBucketManagerImpl;
import com.barco.model.ApplicationDecorator;
import com.barco.model.pojo.Authority;
import com.barco.model.pojo.User;
import com.barco.model.service.NotificationClientService;
import com.barco.model.service.UserService;
import com.barco.model.util.ModelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Nabeel.amd
 */
@Service
@Scope("prototype")
public class CustomUserDetailsService implements UserDetailsService {

    public Logger logger = LogManager.getLogger(CustomUserDetailsService.class);

    private final String BUCKET_NAME = "xyz";
    private final String FOLDER_PATH = "/user/profile/";
    private final String PREFIX_ROLE = "ROLE_";

    @Autowired
    private ModelUtil modelUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationClientService notificationClientService;
    @Autowired
    private AwsBucketManagerImpl awsBucketManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = this.userService.findByUsernameAndStatus(username);
        if (!user.isPresent()) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return user.get();
        }
    }

    public Boolean isUserExist(String username) {
        return this.userService.findByUsernameAndStatus(username).isPresent();
    }

    public void saveUserProfile(ApplicationDecorator decorator) throws Exception {
        UserRequest userRequest = (UserRequest) decorator.getDataBean();
        // upload file first and make the url
        AwsBucketObjectDetail awsBucketObjectDetail = this.awsBucketManager.uploadToBucket(BUCKET_NAME, FOLDER_PATH.concat(String.valueOf(UUID.randomUUID())), userRequest.getFile().getInputStream());
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(this.passwordEncoder.encode(userRequest.getPassword()));
        user.setCompanyName(userRequest.getCompanyName());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setMobile(userRequest.getMobile());
        user.setAddress(userRequest.getAddress());
        user.setImageUrl(awsBucketObjectDetail.getObjKey());
        // find the role by name if user have wrong role then user by defult
        Authority authority = this.userService.getAuthorityByRoleName(PREFIX_ROLE.concat(userRequest.getRole().toUpperCase()));
        if(authority == null) {
            // role user by default whihc is set on the 1 id
            authority = this.userService.getAuthorityById(1L);
        }
        List<Authority> authorities = new ArrayList<>();
        authorities.add(authority);
        // user authority added into user
        user.setAuthorities(authorities);
        // try to save the user
        user = this.userService.saveUserProfile(user);
        // create the new notification
    }

}
