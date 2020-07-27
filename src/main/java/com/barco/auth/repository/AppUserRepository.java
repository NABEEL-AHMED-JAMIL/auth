package com.barco.auth.repository;

import com.barco.model.enums.Status;
import com.barco.model.pojo.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

     Optional<AppUser> findByUsername(String username);

     Optional<AppUser> findByUsernameAndStatus(String username, Status status);

     AppUser findByUsernameAndStatusNot(String username, Status status);

     Optional<AppUser> findByUsernameIgnoreCaseAndStatusNot(String username, Status status);

     AppUser findByIdAndStatusNot(Long userId, Status status);

}
