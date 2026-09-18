package com.markethub.wishlist;
import org.springframework.http.HttpStatus; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/wishlist")
public class WishlistController {
 private final WishlistService service; public WishlistController(WishlistService service){this.service=service;}
 @PostMapping("/{productId}") @ResponseStatus(HttpStatus.CREATED) public void add(Authentication a,@PathVariable Long productId){service.add(a.getName(),productId);}
 @DeleteMapping("/{productId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(Authentication a,@PathVariable Long productId){service.remove(a.getName(),productId);}
 @GetMapping public WishlistPageResponse list(Authentication a,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){if(page<0)throw new InvalidWishlistRequestException("Page cannot be negative");if(size<1)throw new InvalidWishlistRequestException("Size must be at least 1");return service.list(a.getName(),page,Math.min(size,100));}
}