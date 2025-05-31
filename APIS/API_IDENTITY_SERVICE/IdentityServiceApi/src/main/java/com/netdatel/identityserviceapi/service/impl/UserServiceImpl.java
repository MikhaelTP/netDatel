package com.netdatel.identityserviceapi.service.impl;

import com.netdatel.identityserviceapi.domain.dto.AutoRegisterRequest;
import com.netdatel.identityserviceapi.domain.dto.AutoRegisterResponse;
import com.netdatel.identityserviceapi.domain.dto.RoleDto;
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

import java.security.SecureRandom;
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

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int PASSWORD_LENGTH = 8;


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
            authorities.add(new SimpleGrantedAuthority(role.getName()));

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
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        // Inicializar la colección de roles si es null
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        // Asignar roles explícitamente desde los IDs proporcionados en el DTO
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (RoleDto roleDto : userDto.getRoles()) {
                if (roleDto.getId() != null) {
                    Role role = roleRepository.findById(roleDto.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleDto.getId()));
                    roles.add(role);
                }
            }
            user.setRoles(roles);

            // Añadir logs para depuración
            log.debug("Asignados {} roles al usuario {}", roles.size(), userDto.getUsername());
        }

        // Si después de todo no se asignaron roles, asignar rol predeterminado
        if (user.getRoles().isEmpty()) {
            Optional<Role> defaultRole = roleRepository.findByIsDefaultTrue();
            if (defaultRole.isPresent()) {
                user.getRoles().add(defaultRole.get());
                log.debug("Asignado rol predeterminado {} al usuario {}", defaultRole.get().getName(), userDto.getUsername());
            } else {
                log.warn("No se encontró un rol predeterminado para asignar al usuario {}", userDto.getUsername());
            }
        }

        User savedUser = userRepository.save(user);
        // Añadir log para confirmar los roles asignados
        log.info("Usuario creado: {} con {} roles", savedUser.getUsername(), savedUser.getRoles().size());

        auditService.logEvent("USER_CREATED", "user", savedUser.getId().toString(), null, userMapper.toDto(savedUser));

        return userMapper.toDto(savedUser);
    }


    @Override
    @Transactional
    public AutoRegisterResponse autoRegisterUser(AutoRegisterRequest request) {
        // Validate email doesn't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Generate username from email
        String baseUsername = generateUsernameFromEmail(request.getEmail());
        String uniqueUsername = generateUniqueUsername(baseUsername);

        // Generate random password
        String temporaryPassword = generateRandomPassword();

        // Create user entity
        User user = User.builder()
                .username(uniqueUsername)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .userType(request.getUserType())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .attributes(request.getAttributes())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (AutoRegisterRequest.RoleAssignment roleAssignment : request.getRoles()) {
                Role role = roleRepository.findById(roleAssignment.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleAssignment.getId()));
                roles.add(role);
            }
        } else {
            // Assign default role if no roles specified
            Optional<Role> defaultRole = roleRepository.findByIsDefaultTrue();
            defaultRole.ifPresent(roles::add);
        }
        user.setRoles(roles);

        // Save user
        User savedUser = userRepository.save(user);

        // Audit log
        auditService.logEvent("USER_AUTO_REGISTERED", "user", savedUser.getId().toString(),
                null, userMapper.toDto(savedUser));

        log.info("Auto-registered new user: {} with email: {}", savedUser.getUsername(), savedUser.getEmail());

        return AutoRegisterResponse.success(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                temporaryPassword,
                savedUser.getUserType().name()
        );
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

        // IMPORTANTE: Guardar valores críticos ANTES de cualquier mapeo
        String originalPasswordHash = existingUser.getPasswordHash();
        boolean originalEnabled = existingUser.isEnabled();
        boolean originalAccountNonLocked = existingUser.isAccountNonLocked();
        Set<Role> originalRoles = new HashSet<>(existingUser.getRoles());
        LocalDateTime originalCreatedAt = existingUser.getCreatedAt();
        LocalDateTime originalLastLogin = existingUser.getLastLogin();

        // Actualizar solo los campos básicos permitidos
        if (userDto.getUsername() != null) {
            existingUser.setUsername(userDto.getUsername());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getFirstName() != null) {
            existingUser.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            existingUser.setLastName(userDto.getLastName());
        }
        if (userDto.getUserType() != null) {
            existingUser.setUserType(userDto.getUserType());
        }
        if (userDto.getAttributes() != null) {
            existingUser.setAttributes(userDto.getAttributes());
        }

        // RESTAURAR valores críticos que NO deben cambiar con update básico
        existingUser.setPasswordHash(originalPasswordHash);
        existingUser.setEnabled(originalEnabled);
        existingUser.setAccountNonLocked(originalAccountNonLocked);
        existingUser.setRoles(originalRoles);
        existingUser.setCreatedAt(originalCreatedAt);
        existingUser.setLastLogin(originalLastLogin);

        // Solo actualizar password si se proporciona uno nuevo
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
            log.info("Password updated for user: {}", existingUser.getUsername());
        }

        User updatedUser = userRepository.save(existingUser);

        auditService.logEvent("USER_UPDATED", "user", updatedUser.getId().toString(),
                oldUserState, userMapper.toDto(updatedUser));

        log.info("Updated user: {} (enabled: {}, roles: {})",
                updatedUser.getUsername(), updatedUser.isEnabled(), updatedUser.getRoles().size());

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

    // MÉTODOS AUXILIARES PARA AUTO-REGISTRO

    /**
     * Generates a base username from email by removing domain and common extensions
     */
    private String generateUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        String localPart = email.split("@")[0];

        // Clean up the local part - remove dots, plus signs, etc.
        String cleanUsername = localPart
                .replaceAll("[^a-zA-Z0-9]", "") // Remove special characters
                .toLowerCase();

        // Ensure minimum length
        if (cleanUsername.length() < 3) {
            cleanUsername = cleanUsername + "user";
        }

        // Ensure maximum length
        if (cleanUsername.length() > 45) {
            cleanUsername = cleanUsername.substring(0, 45);
        }

        return cleanUsername;
    }

    /**
     * Generates a unique username by adding suffixes if needed
     */
    private String generateUniqueUsername(String baseUsername) {
        String candidateUsername = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(candidateUsername)) {
            candidateUsername = baseUsername + counter;
            counter++;

            // Prevent infinite loop
            if (counter > 9999) {
                candidateUsername = baseUsername + UUID.randomUUID().toString().substring(0, 4);
                break;
            }
        }

        return candidateUsername;
    }

    /**
     * Generates a random 8-digit password
     */
    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int digit = RANDOM.nextInt(10); // 0-9
            password.append(digit);
        }

        return password.toString();
    }
}