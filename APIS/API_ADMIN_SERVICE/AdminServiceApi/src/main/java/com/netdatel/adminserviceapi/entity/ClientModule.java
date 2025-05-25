package com.netdatel.adminserviceapi.entity;

import com.netdatel.adminserviceapi.entity.enums.ModuleStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_modules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "module_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleStatus status = ModuleStatus.ACTIVE;

    @Column(name = "max_user_accounts", nullable = false)
    private Integer maxUserAccounts = 10;

    @Column(name = "specific_storage_limit")
    private Long specificStorageLimit;

    @Column(name = "activation_date")
    private LocalDateTime activationDate;

    @Column(name = "deactivation_date")
    private LocalDateTime deactivationDate;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "last_update")
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "configuration", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configuration;
}