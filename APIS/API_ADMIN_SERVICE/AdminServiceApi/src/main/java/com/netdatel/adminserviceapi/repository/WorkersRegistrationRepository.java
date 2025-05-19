package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.WorkersRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkersRegistrationRepository extends JpaRepository<WorkersRegistration, Integer> {

    List<WorkersRegistration> findByClientId(Integer clientId);

    Optional<WorkersRegistration> findByIdAndClientId(Integer id, Integer clientId);

    boolean existsByClientIdAndEmail(Integer clientId, String email);

    void deleteByClientId(Integer clientId);
}
