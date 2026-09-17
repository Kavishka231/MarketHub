package com.markethub.order;
import com.markethub.common.ApiErrorResponse; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class OrderExceptionHandler {
 @ExceptionHandler({EmptyCartException.class,CheckoutConflictException.class}) ResponseEntity<ApiErrorResponse> conflict(RuntimeException e){return response(HttpStatus.CONFLICT,e);}
 @ExceptionHandler(OrderAccessDeniedException.class) ResponseEntity<ApiErrorResponse> forbidden(RuntimeException e){return response(HttpStatus.FORBIDDEN,e);}
 private ResponseEntity<ApiErrorResponse> response(HttpStatus s,RuntimeException e){return ResponseEntity.status(s).body(new ApiErrorResponse(s.value(),e.getMessage(),null));}
}
