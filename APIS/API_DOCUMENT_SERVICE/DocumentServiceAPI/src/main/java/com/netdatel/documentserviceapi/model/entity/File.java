package com.netdatel.documentserviceapi.model.entity;

import com.netdatel.documentserviceapi.model.enums.FileStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatusColor;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 127)
    private String mimeType;

    @Column(nullable = false, length = 1000)
    private String storagePath;

    @Column(nullable = false, length = 255)
    private String storageKey;

    @Column(length = 255)
    private String hashValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FileStatus status = FileStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ViewStatus viewStatus = ViewStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ViewStatusColor viewStatusColor = ViewStatusColor.BLUE;

    @Column
    private LocalDateTime uploadDate;

    @Column
    private LocalDateTime lastViewedDate;

    @Column
    private LocalDateTime lastDownloadedDate;

    @Column(nullable = false)
    private Integer uploadedBy;

    @Column
    private Integer version = 1;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}