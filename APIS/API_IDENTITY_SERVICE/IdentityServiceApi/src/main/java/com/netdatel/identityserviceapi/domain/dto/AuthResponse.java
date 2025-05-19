package com.netdatel.identityserviceapi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Integer userId;
    private String username;
    private String userType;
    private List<String> roles;
    private List<String> permissions;
}
