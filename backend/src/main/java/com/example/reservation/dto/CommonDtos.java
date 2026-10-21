package com.example.reservation.dto;

import com.example.reservation.domain.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public final class CommonDtos {
    private CommonDtos() {}

    public record ApiError(String message, Map<String, String> errors) {}

    public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> of(Page<?> page, List<T> items) {
            return new PageResponse<>(items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }

    public record LoginRequest(
            @NotBlank(message = "ログインIDを入力してください") String loginId,
            @NotBlank(message = "パスワードを入力してください") String password) {}

    public record UserResponse(Long id, String loginId, Role role, @JsonProperty("is_active") boolean active) {}

    public record MeResponse(UserResponse user, StudentDtos.StudentResponse student) {}
}
