package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.ClientModule;
import com.netdatel.adminserviceapi.entity.enums.ModuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientModuleRepository extends JpaRepository<ClientModule, Integer> {

    List<ClientModule> findByClientId(Integer clientId);

    Optional<ClientModule> findByClientIdAndModuleId(Integer clientId, Integer moduleId);

    @Query("SELECT cm FROM ClientModule cm WHERE cm.status = 'ACTIVE' AND cm.endDate < :currentDate")
    List<ClientModule> findExpiredModules(@Param("currentDate") LocalDate currentDate);

    long countByModuleIdAndStatus(Integer moduleId, ModuleStatus status);

    void deleteByClientId(Integer clientId);
}