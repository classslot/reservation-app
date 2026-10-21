package com.example.reservation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** reservations（予約）: (student_id, slot_id) は一意。取消後の再予約は同じ行を更新する */
@Entity
@Table(name = "reservations",
        uniqueConstraints = @UniqueConstraint(name = "uq_reservation_student_slot", columnNames = {"student_id", "slot_id"}))
public class Reservation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    protected Reservation() {}

    public Reservation(Student student, Slot slot, LocalDateTime reservedAt) {
        this.student = student;
        this.slot = slot;
        this.status = ReservationStatus.CONFIRMED;
        this.reservedAt = reservedAt;
    }

    public Student getStudent() { return student; }
    public Slot getSlot() { return slot; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }

    public void confirm(LocalDateTime now) {
        this.status = ReservationStatus.CONFIRMED;
        this.reservedAt = now;
        this.cancelledAt = null;
    }

    public void cancel(LocalDateTime now) {
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = now;
    }
}
