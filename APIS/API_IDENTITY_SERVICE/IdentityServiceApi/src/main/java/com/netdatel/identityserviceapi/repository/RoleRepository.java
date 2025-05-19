package com.netdatel.identityserviceapi.repository;

import com.netdatel.identityserviceapi.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    Optional<Role> findByIsDefaultTrue();
}
