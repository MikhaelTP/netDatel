package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.Notification;
import com.netdatel.adminserviceapi.entity.enums.NotificationStatus;
import com.netdatel.adminserviceapi.entity.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer>, JpaSpecificationExecutor<Notification> {

    Page<Notification> findByClientIdOrderBySendDateDesc(Long clientId, Pageable pageable);

    Page<Notification> findByTargetTypeOrderBySendDateDesc(TargetType targetType, Pageable pageable);

    Page<Notification> findByStatusOrderBySendDateDesc(NotificationStatus status, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries AND n.lastRetry < :cutoffTime")
    List<Notification> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries, @Param("cutoffTime") LocalDateTime cutoffTime);
}