package com.netdatel.identityserviceapi.api;

import com.netdatel.identityserviceapi.domain.dto.AutoRegisterRequest;
import com.netdatel.identityserviceapi.domain.dto.AutoRegisterResponse;
import com.netdatel.identityserviceapi.domain.dto.UserDto;
import com.netdatel.identityserviceapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        log.debug("REST request to get all Users");
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @securityUtils.isCurrentUserOrAdmin(#id)")
    public ResponseEntity<UserDto> getUser(@PathVariable Integer id) {
        log.debug("REST request to get User : {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/type/{userType}")
    @Operation(summary = "Get users by type")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByType(@PathVariable String userType) {
        log.debug("REST request to get Users by type : {}", userType);
        return ResponseEntity.ok(userService.getUsersByType(userType));
    }

    @PostMapping
    @Operation(summary = "Create new user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        log.debug("REST request to create User : {}", userDto);
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @securityUtils.isCurrentUserOrAdmin(#id)")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserDto userDto) {
        log.debug("REST request to update User : {}", id);
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    // NUEVO ENDPOINT DE AUTO-REGISTRO
    @PostMapping("/auto-register")
    @Operation(summary = "Auto register user with generated credentials",
            description = "Creates a user with auto-generated username and password based on email")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public ResponseEntity<AutoRegisterResponse> autoRegisterUser(@Valid @RequestBody AutoRegisterRequest request) {
        log.debug("REST request to auto-register User with email: {}", request.getEmail());
        return ResponseEntity.ok(userService.autoRegisterUser(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        log.debug("REST request to delete User : {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Add role to user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> addRoleToUser(
            @PathVariable Integer userId,
            @PathVariable Integer roleId) {
        log.debug("REST request to add Role {} to User {}", roleId, userId);
        userService.addRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Remove role from user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable Integer userId,
            @PathVariable Integer roleId) {
        log.debug("REST request to remove Role {} from User {}", roleId, userId);
        userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "Enable user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable Integer id) {
        log.debug("REST request to enable User : {}", id);
        userService.enableUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "Disable user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> disableUser(@PathVariable Integer id) {
        log.debug("REST request to disable User : {}", id);
        userService.disableUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unlock")
    @Operation(summary = "Unlock user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> unlockUser(@PathVariable Integer id) {
        log.debug("REST request to unlock User : {}", id);
        userService.unlockUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/lock")
    @Operation(summary = "Lock user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> lockUser(@PathVariable Integer id) {
        log.debug("REST request to lock User : {}", id);
        userService.lockUser(id);
        return ResponseEntity.ok().build();
    }
}
