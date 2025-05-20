package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.FileAccessHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileAccessHistoryRepository extends JpaRepository<FileAccessHistory, Integer> {
    List<FileAccessHistory> findByFileIdOrderByActionDateDesc(Integer fileId);
    List<FileAccessHistory> findByUserIdOrderByActionDateDesc(Integer userId);

    @Query("SELECT f FROM FileAccessHistory f WHERE f.userId = :userId AND f.actionDate BETWEEN :startDate AND :endDate")
    List<FileAccessHistory> findByUserIdAndDateRange(Integer userId, LocalDateTime startDate, LocalDateTime endDate);
}