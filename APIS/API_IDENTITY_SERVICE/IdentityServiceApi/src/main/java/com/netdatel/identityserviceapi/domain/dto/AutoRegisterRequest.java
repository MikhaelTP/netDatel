package com.netdatel.identityserviceapi.domain.dto;

import com.netdatel.identityserviceapi.domain.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoRegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "User type is required")
    private UserType userType;

    private String firstName;

    private String lastName;

    private String attributes;

    private Set<RoleAssignment> roles = new HashSet<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleAssignment {
        private Integer id;
    }
}