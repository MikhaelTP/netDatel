package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.FilePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilePermissionRepository extends JpaRepository<FilePermission, Integer> {
    List<FilePermission> findByFileId(Integer fileId);
    Optional<FilePermission> findByFileIdAndUserId(Integer fileId, Integer userId);
    boolean existsByFileIdAndUserId(Long fileId, Integer userId);
}