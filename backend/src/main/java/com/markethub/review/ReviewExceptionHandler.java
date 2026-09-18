package com.markethub.review;
import com.markethub.common.ApiErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class ReviewExceptionHandler {
 @ExceptionHandler(ReviewNotFoundException.class) ResponseEntity<ApiErrorResponse> missing(RuntimeException e){return response(HttpStatus.NOT_FOUND,e);}
 @ExceptionHandler(DuplicateReviewException.class) ResponseEntity<ApiErrorResponse> duplicate(RuntimeException e){return response(HttpStatus.CONFLICT,e);}
 @ExceptionHandler(ReviewAccessDeniedException.class) ResponseEntity<ApiErrorResponse> forbidden(RuntimeException e){return response(HttpStatus.FORBIDDEN,e);}
 private ResponseEntity<ApiErrorResponse> response(HttpStatus s,RuntimeException e){return ResponseEntity.status(s).body(new ApiErrorResponse(s.value(),e.getMessage(),null));}
}