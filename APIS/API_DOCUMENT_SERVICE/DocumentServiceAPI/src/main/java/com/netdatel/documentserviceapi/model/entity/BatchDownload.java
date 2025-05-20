package com.netdatel.documentserviceapi.model.entity;

import com.netdatel.documentserviceapi.model.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_downloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDownload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BatchStatus status = BatchStatus.PENDING;

    @Column(name = "total_files")
    private Integer totalFiles;

    @Column(name = "processed_files")
    private Integer processedFiles = 0;

    @Column(name = "download_url", length = 1000)
    private String downloadUrl;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime expirationTime;

    @Column(name = "file_size_bytes")
    private Long fileSize;

    @Column(name = "include_subfolders")
    private boolean includeSubfolders = true;

    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}