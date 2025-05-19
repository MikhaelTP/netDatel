package com.netdatel.adminserviceapi.entity;

import com.netdatel.adminserviceapi.entity.enums.AdministratorStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_administrators",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "email"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAdministrator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String dni;

    @Column(name = "identity_user_id", unique = true)
    private Integer identityUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdministratorStatus status = AdministratorStatus.PENDING;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "notification_date")
    private LocalDateTime notificationDate;
}
