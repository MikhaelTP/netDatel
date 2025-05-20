package com.netdatel.documentserviceapi.model.entity;

import com.netdatel.documentserviceapi.model.enums.ActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_access_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileAccessHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Column(nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ActionType actionType;

    @Column
    private LocalDateTime actionDate;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String deviceInfo;

    @Column(columnDefinition = "jsonb")
    private String additionalInfo;

    @PrePersist
    protected void onCreate() {
        actionDate = LocalDateTime.now();
    }
}