package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.Client;
import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByCode(String code);

    Optional<Client> findByRuc(String ruc);

    List<Client> findByStatus(ClientStatus status);

    Integer countByStatus(ClientStatus status);

    @Query("SELECT c FROM Client c WHERE LOWER(c.businessName) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.commercialName) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR c.ruc LIKE CONCAT('%', :term, '%') " +
            "OR c.code LIKE CONCAT('%', :term, '%')")
    List<Client> search(@Param("term") String term);

    boolean existsByRuc(String ruc);

    List<Client> findTop10ByOrderByRegistrationDateDesc();

    @Query("SELECT MAX(c.code) FROM Client c WHERE c.code LIKE CONCAT(:prefix, '%')")
    String findLatestCodeByPrefix(@Param("prefix") String prefix);

    /*
            "(SELECT COALESCE(SUM(cs.usedBytes), 0) FROM ClientSpace cs WHERE cs.clientId = c.id) " +
            "FROM Client c " +
            "ORDER BY c.businessName")
    List<Object[]> getStorageUsageByClient();
    */
}