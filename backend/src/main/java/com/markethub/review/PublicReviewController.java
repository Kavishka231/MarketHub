package com.markethub.review;
import com.markethub.product.InvalidProductRequestException;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/products/{productId}/reviews")
public class PublicReviewController {
 private final PublicReviewService service; public PublicReviewController(PublicReviewService s){service=s;}
 @GetMapping public ReviewPageResponse list(@PathVariable Long productId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){
  if(page<0)throw new InvalidProductRequestException("Page cannot be negative");
  if(size<1)throw new InvalidProductRequestException("Size must be at least 1");
  return service.list(productId,page,Math.min(size,100));
 }
}