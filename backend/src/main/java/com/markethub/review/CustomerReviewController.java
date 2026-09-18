package com.markethub.review;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/products/{productId}/reviews")
public class CustomerReviewController {
 private final ReviewService service; public CustomerReviewController(ReviewService s){service=s;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ReviewResponse create(Authentication a,@PathVariable Long productId,@Valid @RequestBody ReviewRequest r){return service.create(a.getName(),productId,r);}
 @PutMapping("/me") public ReviewResponse update(Authentication a,@PathVariable Long productId,@Valid @RequestBody ReviewRequest r){return service.update(a.getName(),productId,r);}
 @DeleteMapping("/me") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(Authentication a,@PathVariable Long productId){service.delete(a.getName(),productId);}
}