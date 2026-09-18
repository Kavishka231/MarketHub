package com.markethub.wishlist;
import com.markethub.common.ApiErrorResponse; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class WishlistExceptionHandler {
 @ExceptionHandler(WishlistItemNotFoundException.class) ResponseEntity<ApiErrorResponse> missing(RuntimeException e){return response(HttpStatus.NOT_FOUND,e);}
 @ExceptionHandler(DuplicateWishlistItemException.class) ResponseEntity<ApiErrorResponse> duplicate(RuntimeException e){return response(HttpStatus.CONFLICT,e);}
 @ExceptionHandler(WishlistAccessDeniedException.class) ResponseEntity<ApiErrorResponse> forbidden(RuntimeException e){return response(HttpStatus.FORBIDDEN,e);}
 private ResponseEntity<ApiErrorResponse> response(HttpStatus s,RuntimeException e){return ResponseEntity.status(s).body(new ApiErrorResponse(s.value(),e.getMessage(),null));}
}