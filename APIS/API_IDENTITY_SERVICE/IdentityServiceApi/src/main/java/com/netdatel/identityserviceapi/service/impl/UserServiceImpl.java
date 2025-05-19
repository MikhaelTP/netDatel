package com.netdatel.identityserviceapi.service.impl;

import com.netdatel.identityserviceapi.domain.dto.UserDto;
import com.netdatel.identityserviceapi.domain.entity.Role;
import com.netdatel.identityserviceapi.domain.entity.User;
import com.netdatel.identityserviceapi.domain.entity.UserType;
import com.netdatel.identityserviceapi.domain.mapper.UserMapper;
import com.netdatel.identityserviceapi.exception.ResourceNotFoundException;
import com.netdatel.identityserviceapi.repository.RoleRepository;
import com.netdatel.identityserviceapi.repository.UserRepository;
import com.netdatel.identityserviceapi.service.AuditService;
import com.netdatel.identityserviceapi.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }

        if (!user.isAccountNonLocked()) {
            throw new UsernameNotFoundException("User is locked: " + username);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Add roles as authorities
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Add permissions from roles as authorities
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getCode())));
        });

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isEnabled(),
                true,
                true,
                user.isAccountNonLocked(),
                authorities
        );
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.toEntity(userDto);
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));

        // Assign default role if no roles are specified
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Optional<Role> defaultRole = roleRepository.findByIsDefaultTrue();
            defaultRole.ifPresent(role -> user.setRoles(Set.of(role)));
        }

        User savedUser = userRepository.save(user);
        auditService.logEvent("USER_CREATED", "user", savedUser.getId().toString(), null, userMapper.toDto(savedUser));

        log.info("Created new user: {}", savedUser.getUsername());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Integer id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Save old state for audit
        UserDto oldUserState = userMapper.toDto(existingUser);

        // Check if username is being changed and is already taken
        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
                userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email is being changed and is already taken
        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        userMapper.updateEntityFromDto(userDto, existingUser);

        // Update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);

        auditService.logEvent("USER_UPDATED", "user", updatedUser.getId().toString(), oldUserState, userMapper.toDto(updatedUser));

        log.info("Updated user: {}", updatedUser.getUsername());
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);

        auditService.logEvent("USER_DELETED", "user", id.toString(), userMapper.toDto(user), null);

        log.info("Deleted user: {}", user.getUsername());
    }

    @Override
    public UserDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return userMapper.toDto(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return userMapper.toDto(user);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public List<UserDto> getUsersByType(String userType) {
        try {
            UserType type = UserType.valueOf(userType.toUpperCase());
            return userRepository.findByUserType(type)
                    .stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void addRoleToUser(Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        // Check if user already has the role
        if (user.getRoles().contains(role)) {
            return;
        }

        user.getRoles().add(role);
        userRepository.save(user);

        auditService.logEvent("ROLE_ADDED_TO_USER", "user_role", userId + "_" + roleId, null, null);

        log.info("Added role {} to user {}", role.getName(), user.getUsername());
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        user.getRoles().remove(role);
        userRepository.save(user);

        auditService.logEvent("ROLE_REMOVED_FROM_USER", "user_role", userId + "_" + roleId, null, null);

        log.info("Removed role {} from user {}", role.getName(), user.getUsername());
    }

    @Override
    @Transactional
    public void enableUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEnabled(true);
        userRepository.save(user);

        auditService.logEvent("USER_ENABLED", "user", id.toString(), null, null);

        log.info("Enabled user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void disableUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEnabled(false);
        userRepository.save(user);

        auditService.logEvent("USER_DISABLED", "user", id.toString(), null, null);

        log.info("Disabled user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void unlockUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setAccountNonLocked(true);
        userRepository.save(user);

        auditService.logEvent("USER_UNLOCKED", "user", id.toString(), null, null);

        log.info("Unlocked user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void lockUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setAccountNonLocked(false);
        userRepository.save(user);

        auditService.logEvent("USER_LOCKED", "user", id.toString(), null, null);

        log.info("Locked user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void updateLastLogin(Integer userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
        log.debug("Updated last login time for user ID: {}", userId);
    }
}