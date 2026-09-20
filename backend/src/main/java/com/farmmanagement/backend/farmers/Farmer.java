package com.farmmanagement.backend.farmers;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "farmers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_farmer_code",
                        columnNames = "farmer_code"
                )
        }
)
public class Farmer {

    public enum FarmerStatus {
        ACTIVE,
        INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_code", nullable = false, unique = true, length = 50)
    private String farmerCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String village;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FarmerStatus status = FarmerStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getFarmerCode() {
        return farmerCode;
    }

    public void setFarmerCode(String farmerCode) {
        this.farmerCode = farmerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public FarmerStatus getStatus() {
        return status;
    }

    public void setStatus(FarmerStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}