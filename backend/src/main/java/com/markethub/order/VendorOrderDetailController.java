package com.markethub.order;
import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/vendor/orders") public class VendorOrderDetailController {private final VendorOrderDetailService service;public VendorOrderDetailController(VendorOrderDetailService s){service=s;}@GetMapping("/{orderId}")public VendorOrderDetailResponse get(Authentication a,@PathVariable Long orderId){return service.get(a.getName(),orderId);}}
