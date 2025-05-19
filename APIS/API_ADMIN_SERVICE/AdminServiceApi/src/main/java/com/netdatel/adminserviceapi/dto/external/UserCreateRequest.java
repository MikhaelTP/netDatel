package com.netdatel.adminserviceapi.dto.external;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCreateRequest {
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String userType;
    private UserAttributes attributes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAttributes {
        private Integer clientId;
        private java.util.List<String> modules;
    }
}