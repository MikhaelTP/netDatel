package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.BatchDownload;
import com.netdatel.documentserviceapi.model.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchDownloadRepository extends JpaRepository<BatchDownload, Integer> {
    List<BatchDownload> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<BatchDownload> findByStatusOrderByCreatedAtDesc(BatchStatus status);
}