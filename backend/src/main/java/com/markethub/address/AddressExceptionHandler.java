package com.markethub.address;

import com.markethub.common.ApiErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class AddressExceptionHandler {
    @ExceptionHandler(AddressNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(AddressNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler({AddressAccessDeniedException.class, AddressOwnershipException.class})
    ResponseEntity<ApiErrorResponse> forbidden(RuntimeException exception) {
        return response(HttpStatus.FORBIDDEN, exception);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, RuntimeException exception) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), exception.getMessage(), null));
    }
}
