package com.example.reservation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** 全テーブル共通の id / created_at / updated_at */
@MappedSuperclass
public abstract class BaseEntity {
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(JST);
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(JST);
    }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
