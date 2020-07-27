package com.barco.auth.repository;

import com.barco.model.pojo.AppUser;
import com.barco.model.pojo.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {

    UserVerification findByAppUser(AppUser appUser);

    UserVerification findByToken(String token);
}
