package com.markethub.admin;
import com.markethub.common.ApiErrorResponse; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class AdminExceptionHandler{
 @ExceptionHandler(AdminNotFoundException.class) ResponseEntity<ApiErrorResponse> missing(RuntimeException e){return response(HttpStatus.NOT_FOUND,e);}
 @ExceptionHandler(AdminConflictException.class) ResponseEntity<ApiErrorResponse> conflict(RuntimeException e){return response(HttpStatus.CONFLICT,e);}
 private ResponseEntity<ApiErrorResponse> response(HttpStatus s,RuntimeException e){return ResponseEntity.status(s).body(new ApiErrorResponse(s.value(),e.getMessage(),null));}}