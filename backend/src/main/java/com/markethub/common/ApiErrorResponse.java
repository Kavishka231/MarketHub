package com.markethub.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String requestId,
        Map<String, String> errors) {

    public ApiErrorResponse(int status, String message, Map<String, String> errors) {
        this(
                Instant.now(),
                status,
                statusName(status),
                null,
                message,
                null,
                null,
                errors);
    }

    private static String statusName(int status) {
        return org.springframework.http.HttpStatus.valueOf(status).getReasonPhrase();
    }
}