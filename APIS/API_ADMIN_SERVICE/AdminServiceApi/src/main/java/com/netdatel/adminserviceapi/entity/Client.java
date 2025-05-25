package com.netdatel.adminserviceapi.entity;

import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
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
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(unique = true, nullable = false, length = 20)
    private String ruc;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "commercial_name", length = 200)
    private String commercialName;

    @Column(name = "taxpayer_type", length = 50)
    private String taxpayerType;

    @Column(name = "activity_start_date")
    private LocalDate activityStartDate;

    @Column(name = "fiscal_address", length = 300)
    private String fiscalAddress;

    @Column(name = "economic_activity", length = 300)
    private String economicActivity;

    @Column(name = "contact_number", length = 50)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClientStatus status = ClientStatus.ACTIVE;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = ClientStatus.ACTIVE;
        }
    }

    @Column(nullable = false)
    private Boolean notified = false;

    @Column(name = "allocated_storage")
    private Long allocatedStorage = 0L;

    @Column(name = "registration_date")
    @CreationTimestamp
    private LocalDateTime registrationDate;

    @Column(name = "last_update_date")
    @UpdateTimestamp
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LegalRepresentative> legalRepresentatives = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientModule> clientModules = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientAdministrator> administrators = new ArrayList<>();

    // Métodos helper para relaciones bidireccionales
    public void addLegalRepresentative(LegalRepresentative representative) {
        legalRepresentatives.add(representative);
        representative.setClient(this);
    }

    public void removeLegalRepresentative(LegalRepresentative representative) {
        legalRepresentatives.remove(representative);
        representative.setClient(null);
    }

    public void addClientModule(ClientModule module) {
        clientModules.add(module);
        module.setClient(this);
    }

    public void removeClientModule(ClientModule module) {
        clientModules.remove(module);
        module.setClient(null);
    }

    public void addAdministrator(ClientAdministrator administrator) {
        administrators.add(administrator);
        administrator.setClient(this);
    }

    public void removeAdministrator(ClientAdministrator administrator) {
        administrators.remove(administrator);
        administrator.setClient(null);
    }
}