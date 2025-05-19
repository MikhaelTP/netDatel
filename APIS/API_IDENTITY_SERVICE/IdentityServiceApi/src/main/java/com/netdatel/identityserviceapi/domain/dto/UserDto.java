package com.netdatel.identityserviceapi.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netdatel.identityserviceapi.domain.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Integer id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "User type is required")
    private UserType userType;

    private String firstName;

    private String lastName;

    private boolean enabled;

    private boolean accountNonLocked;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    private String attributes;

    private Set<RoleDto> roles = new HashSet<>();
}
