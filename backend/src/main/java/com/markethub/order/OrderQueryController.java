package com.markethub.order;
import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders")
public class OrderQueryController {private final OrderQueryService service;public OrderQueryController(OrderQueryService s){service=s;}
 @GetMapping public OrderPageResponse list(Authentication a,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return service.list(a.getName(),page,size);}
 @GetMapping("/{orderId}") public OrderDetailResponse get(Authentication a,@PathVariable Long orderId){return service.get(a.getName(),orderId);}
}
