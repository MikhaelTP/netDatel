package com.netdatel.identityserviceapi.repository;

import com.netdatel.identityserviceapi.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByCategory(String category);

    List<Permission> findByService(String service);

    boolean existsByCode(String code);
}
