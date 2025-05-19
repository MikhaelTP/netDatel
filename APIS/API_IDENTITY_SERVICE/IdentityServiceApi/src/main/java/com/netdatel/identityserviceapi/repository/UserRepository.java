package com.netdatel.identityserviceapi.repository;

import com.netdatel.identityserviceapi.domain.entity.User;
import com.netdatel.identityserviceapi.domain.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUserType(UserType userType);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = ?2 WHERE u.id = ?1")
    void updateLastLogin(Integer userId, LocalDateTime loginTime);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = ?1")
    List<User> findByRoleName(String roleName);
}