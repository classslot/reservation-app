package com.example.reservation.common;

import org.springframework.http.HttpStatus;

/** API仕様書のエラー（401/403/404/409/422 など）を表す例外 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND, message); }
    public static ApiException forbidden(String message) { return new ApiException(HttpStatus.FORBIDDEN, message); }
    public static ApiException conflict(String message) { return new ApiException(HttpStatus.CONFLICT, message); }
    public static ApiException invalid(String message) { return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, message); }
}
