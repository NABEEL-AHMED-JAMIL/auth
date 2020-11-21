package com.barco.auth.repository;

import com.barco.model.enums.Status;
import com.barco.model.pojo.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Nabeel Ahmed
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    Optional<Authority> findByRoleIgnoreCaseAndStatus(String role, Status status);

    Optional<Authority> findByRole(String role);

    List<Authority> findAllByStatus(Status status);

}
