package com.markethub.cart;

import com.markethub.common.ApiErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class CartExceptionHandler {
    @ExceptionHandler(CartItemNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(CartItemNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler({ProductUnavailableException.class, InsufficientStockException.class})
    ResponseEntity<ApiErrorResponse> conflict(RuntimeException exception) {
        return response(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler({CartAccessDeniedException.class, CartItemOwnershipException.class})
    ResponseEntity<ApiErrorResponse> forbidden(RuntimeException exception) {
        return response(HttpStatus.FORBIDDEN, exception);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, RuntimeException exception) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), exception.getMessage(), null));
    }
}
