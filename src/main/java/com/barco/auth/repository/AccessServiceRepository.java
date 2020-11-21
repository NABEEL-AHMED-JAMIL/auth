package com.barco.auth.repository;

import com.barco.model.enums.Status;
import com.barco.model.pojo.AccessService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Nabeel Ahmed
 */
@Repository
public interface AccessServiceRepository extends JpaRepository<AccessService, Long> {

    List<AccessService> findAllByStatus(Status status);

    Set<AccessService> findAllByIdInAndStatus(List<Long> ids, Status status);
}
