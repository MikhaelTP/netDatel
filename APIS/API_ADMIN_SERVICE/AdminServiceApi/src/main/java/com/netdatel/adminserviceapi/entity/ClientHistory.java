package com.netdatel.adminserviceapi.entity;

import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(nullable = false, length = 50)
    private String action;

    //@Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = true, length = 20)
    private String  newStatus;

    //@Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = true, length = 20)
    private String  previousStatus;

    @Column(name = "change_date")
    @CreationTimestamp
    private LocalDateTime changeDate;

    @Column(name = "changed_by", nullable = false)
    private Integer changedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "details")
    private String details;
}
