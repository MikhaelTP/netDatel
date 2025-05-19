package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {

    Optional<Module> findByCode(String code);

    List<Module> findByIsActiveTrue();
}
