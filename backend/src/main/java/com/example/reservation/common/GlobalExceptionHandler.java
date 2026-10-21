package com.example.reservation.common;

import com.example.reservation.dto.CommonDtos.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.LinkedHashMap;
import java.util.Map;

/** 例外をAPI仕様書どおりのステータスとJSONメッセージに変換する */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static ResponseEntity<ApiError> body(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ApiError(message, errors));
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApi(ApiException e) {
        return body(e.getStatus(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        String first = errors.values().stream().findFirst().orElse("入力内容に誤りがあります");
        return body(HttpStatus.UNPROCESSABLE_ENTITY, first, errors);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthentication(AuthenticationException e) {
        // 理由（IDなし／パスワード違い／無効アカウント）は区別せず一般的な文言にする
        return body(HttpStatus.UNAUTHORIZED, "ログインIDまたはパスワードが正しくありません", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleDenied(AccessDeniedException e) {
        return body(HttpStatus.FORBIDDEN, "この操作を行う権限がありません", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "リクエストの形式が正しくありません", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return body(HttpStatus.UNPROCESSABLE_ENTITY, "パラメータの形式が正しくありません", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException e) {
        log.warn("データ整合性エラー: {}", e.getMostSpecificCause().getMessage());
        return body(HttpStatus.CONFLICT, "既に登録されているデータと重複しています", null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleOthers(Exception e) throws Exception {
        if (e instanceof ErrorResponse er) { // 404/405 など Spring 標準のHTTPエラー
            return body(HttpStatus.valueOf(er.getStatusCode().value()), "リクエストを処理できません", null);
        }
        // 内部情報は画面に返さず、ログにのみ出力する（パスワード等は出力しない）
        log.error("予期しないエラー", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "サーバーでエラーが発生しました", null);
    }
}
