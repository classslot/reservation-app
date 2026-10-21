package com.example.reservation.repository;

import com.example.reservation.domain.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long>, JpaSpecificationExecutor<Slot> {
    /** 予約・枠更新時に行ロックを取り、定員超過を防ぐ */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);
}
