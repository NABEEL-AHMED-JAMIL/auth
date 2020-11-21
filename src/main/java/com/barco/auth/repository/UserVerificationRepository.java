package com.barco.auth.repository;

import com.barco.model.enums.Status;
import com.barco.model.pojo.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Nabeel Ahmed
 */
@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {

    UserVerification findByCreatedBy(Long appUserId);

    UserVerification findByToken(String token);

    UserVerification findByTokenAndStatus(String token, Status status);
}
