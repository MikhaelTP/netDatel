package com.netdatel.identityserviceapi.repository;

import com.netdatel.identityserviceapi.domain.entity.AuditLog;
import com.netdatel.identityserviceapi.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    /**
     * Find audit logs by user
     */
    Page<AuditLog> findByUser(User user, Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find audit logs by entity type and entity id
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    /**
     * Find audit logs by timestamp range
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Find audit logs by action and timestamp between
     */
    List<AuditLog> findByActionAndTimestampBetween(String action, LocalDateTime start, LocalDateTime end);
}
