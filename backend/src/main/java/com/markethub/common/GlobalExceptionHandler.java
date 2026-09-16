package com.markethub.common;

import com.markethub.auth.AccountDisabledException;
import com.markethub.auth.DuplicateEmailException;
import com.markethub.auth.InvalidCredentialsException;
import com.markethub.category.DuplicateCategoryException;
import com.markethub.category.InvalidCategoryNameException;
import com.markethub.vendor.DuplicateVendorApplicationException;
import com.markethub.vendor.InvalidVendorStatusException;
import com.markethub.vendor.VendorAccessDeniedException;
import com.markethub.vendor.VendorNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledAccount(AccountDisabledException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler({DuplicateVendorApplicationException.class, InvalidVendorStatusException.class})
    public ResponseEntity<ApiErrorResponse> handleVendorConflict(RuntimeException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(VendorNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleVendorNotFound(VendorNotFoundException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(VendorAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleVendorAccessDenied(VendorAccessDeniedException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateCategory(DuplicateCategoryException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCategoryNameException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCategoryName(InvalidCategoryNameException exception) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(), exception.getMessage(), null);
        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put(exception.getName(), "Invalid value: " + exception.getValue());
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Invalid request parameter", fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Validation failed", fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }
}
