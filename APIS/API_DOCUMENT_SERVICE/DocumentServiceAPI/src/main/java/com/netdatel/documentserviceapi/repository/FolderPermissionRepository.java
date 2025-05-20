package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.FolderPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderPermissionRepository extends JpaRepository<FolderPermission, Integer> {
    List<FolderPermission> findByFolderId(Integer folderId);
    Optional<FolderPermission> findByFolderIdAndUserId(Integer folderId, Integer userId);
    boolean existsByFolderIdAndUserId(Integer folderId, Integer userId);

    @Query("SELECT fp FROM FolderPermission fp WHERE fp.folder.id = :folderId AND fp.isActive = true AND (fp.validUntil IS NULL OR fp.validUntil > CURRENT_TIMESTAMP)")
    List<FolderPermission> findActivePermissionsByFolderId(Integer folderId);
}