package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.ClientAdministrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAdministratorRepository extends JpaRepository<ClientAdministrator, Integer> {

    List<ClientAdministrator> findByClientId(Integer clientId);

    Optional<ClientAdministrator> findByClientIdAndEmail(Integer clientId, String email);

    @Query("SELECT ca FROM ClientAdministrator ca WHERE ca.client.id = :clientId AND ca.status = 'ACTIVE'")
    List<ClientAdministrator> findByClientIdAndStatusActive(@Param("clientId") Integer clientId);

    boolean existsByEmail(String email);

    void deleteByClientId(Integer clientId);
}
