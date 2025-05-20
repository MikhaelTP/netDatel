package com.netdatel.documentserviceapi.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileVersionResponse {
    private Integer id;
    private Integer fileId;
    private Integer versionNumber;
    private Long fileSize;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private String changeComments;
}