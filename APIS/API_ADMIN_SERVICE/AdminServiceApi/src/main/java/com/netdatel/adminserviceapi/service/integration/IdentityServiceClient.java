package com.netdatel.adminserviceapi.service.integration;

import com.netdatel.adminserviceapi.dto.external.RoleAssignmentRequest;
import com.netdatel.adminserviceapi.dto.external.UserCreateRequest;
import com.netdatel.adminserviceapi.dto.external.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "identity-service", url = "${app.services.identity-url}")
public interface IdentityServiceClient {

    @PostMapping("/api/users")
    UserResponse createUser(@RequestBody UserCreateRequest request);

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable Integer userId);

    @PostMapping("/api/users/{userId}/roles")
    void assignRoleToUser(
            @PathVariable Integer userId,
            @RequestBody RoleAssignmentRequest request);

    @PutMapping("/api/users/{userId}/enable")
    void enableUser(@PathVariable Integer userId);

    @PutMapping("/api/users/{userId}/disable")
    void disableUser(@PathVariable Integer userId);
}