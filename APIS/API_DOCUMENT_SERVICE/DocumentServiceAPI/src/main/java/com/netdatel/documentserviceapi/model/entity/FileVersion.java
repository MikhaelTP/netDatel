package com.netdatel.documentserviceapi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 1000)
    private String storagePath;

    @Column(nullable = false, length = 255)
    private String storageKey;

    @Column(length = 255)
    private String hashValue;

    @Column
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer createdBy;

    @Column
    private String changeComments;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}