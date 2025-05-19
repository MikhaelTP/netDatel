package com.netdatel.identityserviceapi.service;

import com.netdatel.identityserviceapi.domain.dto.UserDto;
import com.netdatel.identityserviceapi.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Integer id, UserDto userDto);

    void deleteUser(Integer id);

    UserDto getUserById(Integer id);

    UserDto getUserByUsername(String username);

    Optional<User> findUserByUsername(String username);

    Page<UserDto> getAllUsers(Pageable pageable);

    List<UserDto> getUsersByType(String userType);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void addRoleToUser(Integer userId, Integer roleId);

    void removeRoleFromUser(Integer userId, Integer roleId);

    void enableUser(Integer id);

    void disableUser(Integer id);

    void unlockUser(Integer id);

    void lockUser(Integer id);
    /**
     * Update the last login timestamp for a user
     * @param userId ID of the user to update
     */
    void updateLastLogin(Integer userId);
}