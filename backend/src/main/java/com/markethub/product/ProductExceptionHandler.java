package com.markethub.product;

import com.markethub.common.ApiErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ProductExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(ProductNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(ProductOwnershipException.class)
    ResponseEntity<ApiErrorResponse> forbidden(ProductOwnershipException exception) {
        return response(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler({ProductCategoryInactiveException.class, InvalidProductRequestException.class})
    ResponseEntity<ApiErrorResponse> badRequest(RuntimeException exception) {
        return response(HttpStatus.BAD_REQUEST, exception);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, RuntimeException exception) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), exception.getMessage(), null));
    }
}
