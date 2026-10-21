package com.example.reservation.repository;

import com.example.reservation.domain.Reservation;
import com.example.reservation.domain.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    long countBySlot_IdAndStatus(Long slotId, ReservationStatus status);

    /** 枠ごとの確定予約数（[slotId, count] の配列） */
    @Query("select r.slot.id, count(r) from Reservation r "
            + "where r.slot.id in :ids and r.status = :status group by r.slot.id")
    List<Object[]> countBySlotIds(@Param("ids") Collection<Long> ids, @Param("status") ReservationStatus status);

    /** 指定生徒が確定予約している枠ID */
    @Query("select r.slot.id from Reservation r "
            + "where r.student.id = :studentId and r.status = :status and r.slot.id in :ids")
    List<Long> findSlotIdsByStudent(@Param("studentId") Long studentId,
                                    @Param("status") ReservationStatus status,
                                    @Param("ids") Collection<Long> ids);

    Optional<Reservation> findByStudent_IdAndSlot_Id(Long studentId, Long slotId);

    /** 開始前の確定予約があるか（退会可否の判定用） */
    boolean existsByStudent_IdAndStatusAndSlot_StartAtAfter(Long studentId, ReservationStatus status, LocalDateTime now);

    @EntityGraph(attributePaths = {"slot", "student"})
    Page<Reservation> findByStudent_Id(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"slot", "student"})
    Page<Reservation> findByStudent_IdAndStatus(Long studentId, ReservationStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"slot", "student"})
    Page<Reservation> findAll(Specification<Reservation> spec, Pageable pageable);
}
