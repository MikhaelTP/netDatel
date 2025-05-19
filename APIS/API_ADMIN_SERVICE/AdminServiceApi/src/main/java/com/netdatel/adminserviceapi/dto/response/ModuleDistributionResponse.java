package com.netdatel.adminserviceapi.dto.response;

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
public class ModuleDistributionResponse {

    private Integer moduleId;
    private String moduleName;
    private String moduleCode;
    private Long activeSubscriptions;
}