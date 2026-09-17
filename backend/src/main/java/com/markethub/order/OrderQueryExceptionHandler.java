package com.markethub.order;
import com.markethub.common.ApiErrorResponse; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class OrderQueryExceptionHandler {
 @ExceptionHandler(OrderNotFoundException.class) ResponseEntity<ApiErrorResponse> nf(RuntimeException e){return r(HttpStatus.NOT_FOUND,e);}
 @ExceptionHandler(OrderOwnershipException.class) ResponseEntity<ApiErrorResponse> f(RuntimeException e){return r(HttpStatus.FORBIDDEN,e);}
 @ExceptionHandler(InvalidOrderRequestException.class) ResponseEntity<ApiErrorResponse> b(RuntimeException e){return r(HttpStatus.BAD_REQUEST,e);}
 private ResponseEntity<ApiErrorResponse> r(HttpStatus s,RuntimeException e){return ResponseEntity.status(s).body(new ApiErrorResponse(s.value(),e.getMessage(),null));}
}
