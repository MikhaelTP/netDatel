package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Integer> {
    List<FileVersion> findByFileId(Integer fileId);
    List<FileVersion> findByFileIdOrderByVersionNumberDesc(Integer fileId);
    Optional<FileVersion> findByFileIdAndVersionNumber(Integer fileId, Integer versionNumber);
}