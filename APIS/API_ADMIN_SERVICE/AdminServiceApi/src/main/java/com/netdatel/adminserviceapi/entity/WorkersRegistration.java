package com.netdatel.adminserviceapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workers_registration",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "email"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkersRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String dni;

    @Column(name = "identity_user_id")
    private Integer identityUserId;

    @Column(name = "is_registered")
    private Boolean isRegistered = false;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

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
