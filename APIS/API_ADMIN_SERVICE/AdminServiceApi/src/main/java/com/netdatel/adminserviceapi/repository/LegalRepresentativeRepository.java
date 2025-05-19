package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.LegalRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalRepresentativeRepository extends JpaRepository<LegalRepresentative, Integer> {

    List<LegalRepresentative> findByClientId(Long clientId);

    Optional<LegalRepresentative> findByClientIdAndDocumentNumberAndIsActiveTrue(
            Long clientId, String documentNumber);

    void deleteByClientId(Integer clientId);
}
