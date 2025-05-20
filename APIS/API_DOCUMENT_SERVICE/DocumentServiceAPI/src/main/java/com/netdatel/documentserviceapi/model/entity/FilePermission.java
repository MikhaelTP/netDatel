package com.netdatel.documentserviceapi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Column(nullable = false)
    private Integer userId;

    @Column
    private boolean canRead = false;

    @Column
    private boolean canWrite = false;

    @Column
    private boolean canDelete = false;

    @Column
    private boolean canDownload = false;

    @Column
    private LocalDateTime grantedAt;

    @Column(nullable = false)
    private Integer grantedBy;

    @Column
    private LocalDateTime validFrom;

    @Column
    private LocalDateTime validUntil;

    @Column
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
        validFrom = LocalDateTime.now();
    }
}
