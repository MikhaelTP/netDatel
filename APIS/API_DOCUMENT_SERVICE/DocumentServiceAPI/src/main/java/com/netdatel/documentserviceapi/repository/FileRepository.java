package com.netdatel.documentserviceapi.repository;


import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.enums.FileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    List<File> findByFolderId(Integer folderId);
    List<File> findByFolderIdAndStatus(Integer folderId, FileStatus status);
    Optional<File> findByFolderIdAndName(Integer folderId, String name);
    boolean existsByFolderIdAndName(Integer folderId, String name);

    @Query("SELECT f FROM File f WHERE f.folder.clientSpace.clientId = :clientId AND f.name LIKE %:query% OR f.originalName LIKE %:query%")
    Page<File> searchByNameAndClientId(Integer clientId, String query, Pageable pageable);

    @Query(value = "SELECT * FROM document.files WHERE metadata @> :jsonQuery", nativeQuery = true)
    List<File> findByMetadataContaining(String jsonQuery);

    int countByFolderIdAndStatus(Integer id, FileStatus fileStatus);

    Page<File> findByNameContainingIgnoreCase(String query, Pageable pageable);
}