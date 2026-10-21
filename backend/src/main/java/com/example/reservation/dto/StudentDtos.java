package com.example.reservation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class StudentDtos {
    private StudentDtos() {}

    public record StudentResponse(Long id, String name, String guardianName, String contactEmail,
                                  String course, String game, String level, boolean makeupAllowed,
                                  @JsonProperty("is_active") boolean active, String loginId) {}

    public record StudentCreateRequest(
            @NotBlank(message = "氏名を入力してください") @Size(max = 100, message = "氏名は100文字以内で入力してください") String name,
            @NotBlank(message = "保護者氏名を入力してください") @Size(max = 100, message = "保護者氏名は100文字以内で入力してください") String guardianName,
            @NotBlank(message = "連絡先メールアドレスを入力してください") @Email(message = "メールアドレスの形式が正しくありません") String contactEmail,
            @NotBlank(message = "コースを入力してください") @Size(max = 50, message = "コースは50文字以内で入力してください") String course,
            @NotBlank(message = "ゲームを入力してください") @Size(max = 50, message = "ゲームは50文字以内で入力してください") String game,
            @NotBlank(message = "レベルを入力してください") @Size(max = 50, message = "レベルは50文字以内で入力してください") String level,
            @NotBlank(message = "ログインIDを入力してください")
            @Size(min = 4, max = 50, message = "ログインIDは4〜50文字で入力してください")
            @Pattern(regexp = "^[A-Za-z0-9_.-]*$", message = "ログインIDは半角英数字と _ . - が使えます") String loginId,
            @NotBlank(message = "パスワードを入力してください")
            @Size(min = 8, max = 72, message = "パスワードは8〜72文字で入力してください") String password,
            Boolean makeupAllowed) {}

    /** PATCH用。指定した項目だけ更新する */
    public record StudentUpdateRequest(
            @Size(max = 100, message = "氏名は100文字以内で入力してください") String name,
            @Size(max = 100, message = "保護者氏名は100文字以内で入力してください") String guardianName,
            @Email(message = "メールアドレスの形式が正しくありません") String contactEmail,
            @Size(max = 50, message = "コースは50文字以内で入力してください") String course,
            @Size(max = 50, message = "ゲームは50文字以内で入力してください") String game,
            @Size(max = 50, message = "レベルは50文字以内で入力してください") String level,
            Boolean makeupAllowed,
            @JsonProperty("is_active") Boolean active) {}
}
