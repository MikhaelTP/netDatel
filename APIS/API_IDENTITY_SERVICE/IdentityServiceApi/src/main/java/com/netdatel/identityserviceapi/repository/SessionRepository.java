package com.netdatel.identityserviceapi.repository;

import com.netdatel.identityserviceapi.domain.entity.Session;
import com.netdatel.identityserviceapi.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    Optional<Session> findByToken(String token);

    List<Session> findByUser(User user);

    List<Session> findByUserAndIsActiveTrue(User user);

    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.user = ?1")
    void deactivateAllUserSessions(User user);

    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.expiresAt < ?1")
    void deactivateExpiredSessions(LocalDateTime now);
}
