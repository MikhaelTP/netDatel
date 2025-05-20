package com.netdatel.documentserviceapi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "client_space_id", nullable = false)
    private ClientSpace clientSpace;

    @Column(nullable = false, length = 255)
    private String name;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer createdBy;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private Integer updatedBy;

    @Column
    private boolean isActive = true;

    @Column(columnDefinition = "jsonb")
    private String attributes = "{}";

    @PrePersist
    protected void onCreate()
    {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate()
    {
        updatedAt = LocalDateTime.now();
    }
}