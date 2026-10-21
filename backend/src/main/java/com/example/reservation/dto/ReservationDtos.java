package com.example.reservation.dto;

import com.example.reservation.domain.ReservationStatus;
import com.example.reservation.domain.SlotType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class ReservationDtos {
    private ReservationDtos() {}

    public record ReserveRequest(@NotNull(message = "予約枠を指定してください") Long slotId) {}

    public record SlotSummary(Long id, SlotType type, String title, String teacherName,
                              LocalDateTime startAt, LocalDateTime endAt) {}

    public record StudentSummary(Long id, String name, String course, String game, String level) {}

    /** student は参加者情報。会員向けAPIでは null にして他の生徒の情報を返さない */
    public record ReservationResponse(Long id, ReservationStatus status, LocalDateTime reservedAt,
                                      LocalDateTime cancelledAt, SlotSummary slot, StudentSummary student) {}
}
