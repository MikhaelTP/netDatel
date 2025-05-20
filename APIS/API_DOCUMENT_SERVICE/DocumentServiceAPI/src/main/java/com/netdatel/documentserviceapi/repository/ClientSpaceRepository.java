package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.ClientSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientSpaceRepository extends JpaRepository<ClientSpace, Integer> {
    Optional<ClientSpace> findByClientIdAndModuleId(Integer clientId, Integer moduleId);
    List<ClientSpace> findByClientId(Integer clientId);
    boolean existsByClientIdAndModuleId(Integer clientId, Integer moduleId);
}
