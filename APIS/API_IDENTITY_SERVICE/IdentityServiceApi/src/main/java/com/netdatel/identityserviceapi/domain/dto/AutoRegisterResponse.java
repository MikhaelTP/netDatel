package com.netdatel.identityserviceapi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoRegisterResponse {

    private Integer userId;
    private String username;
    private String email;
    private String temporaryPassword;
    private String userType;
    private String message;

    public static AutoRegisterResponse success(Integer userId, String username, String email,
                                               String temporaryPassword, String userType) {
        return AutoRegisterResponse.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .temporaryPassword(temporaryPassword)
                .userType(userType)
                .message("User registered successfully. Please update your credentials using the update endpoint.")
                .build();
    }
}