package com.netdatel.adminserviceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleResponse {

    private Integer id;
    private String code;
    private String name;
    private String description;
    private String version;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;
    private String features;

}
