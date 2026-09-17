package com.markethub.order;
import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders") public class OrderCancellationController {private final OrderCancellationService service;public OrderCancellationController(OrderCancellationService s){service=s;}@PatchMapping("/{orderId}/cancel") public OrderSummaryResponse cancel(Authentication a,@PathVariable Long orderId){return service.cancel(a.getName(),orderId);}}
