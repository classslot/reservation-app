package com.example.reservation.dto;

import com.example.reservation.domain.SlotStatus;
import com.example.reservation.domain.SlotType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class SlotDtos {
    private SlotDtos() {}

    /** remaining は capacity - 確定予約数 で計算した値。reserved はログイン中の会員が予約済みか */
    public record SlotResponse(Long id, SlotType type, String title, String teacherName,
                               LocalDateTime startAt, LocalDateTime endAt,
                               String targetCourse, String targetGame, String targetLevel,
                               int capacity, int remaining, SlotStatus status, boolean reserved) {}

    public record SlotCreateRequest(
            @NotNull(message = "種別を選択してください") SlotType type,
            @NotBlank(message = "タイトルを入力してください") @Size(max = 100, message = "タイトルは100文字以内で入力してください") String title,
            @NotBlank(message = "担当者を入力してください") @Size(max = 100, message = "担当者は100文字以内で入力してください") String teacherName,
            @NotNull(message = "開始日時を入力してください") LocalDateTime startAt,
            @NotNull(message = "終了日時を入力してください") LocalDateTime endAt,
            @NotNull(message = "定員を入力してください") @Min(value = 1, message = "定員は1以上で入力してください") Integer capacity,
            SlotStatus status,
            @Size(max = 50, message = "対象コースは50文字以内で入力してください") String targetCourse,
            @Size(max = 50, message = "対象ゲームは50文字以内で入力してください") String targetGame,
            @Size(max = 50, message = "対象レベルは50文字以内で入力してください") String targetLevel) {}

    /** PATCH用。指定した項目だけ更新する（対象条件は空文字で解除） */
    public record SlotUpdateRequest(SlotType type, String title, String teacherName,
                                    LocalDateTime startAt, LocalDateTime endAt, Integer capacity, SlotStatus status,
                                    String targetCourse, String targetGame, String targetLevel) {}
}
