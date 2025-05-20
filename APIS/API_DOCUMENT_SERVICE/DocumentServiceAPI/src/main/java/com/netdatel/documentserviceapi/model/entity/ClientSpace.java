package com.netdatel.documentserviceapi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_spaces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false)
    private Integer moduleId;

    @Column(nullable = false, length = 255)
    private String storagePath;

    @Column(nullable = false)
    private Long totalQuotaBytes;

    @Column
    private Long usedBytes = 0L;

    @Column
    private boolean isActive = true;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer createdBy;

    @Column
    private Integer updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}