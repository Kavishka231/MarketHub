package com.markethub.order;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders")
public class CheckoutController { private final CheckoutService service; public CheckoutController(CheckoutService s){service=s;}
 @PostMapping("/checkout") @ResponseStatus(HttpStatus.CREATED) public OrderSummaryResponse checkout(Authentication a,@Valid @RequestBody CheckoutRequest r){return service.checkout(a.getName(),r);}
}
