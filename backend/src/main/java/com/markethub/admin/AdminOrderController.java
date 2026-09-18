package com.markethub.admin;
import com.markethub.order.OrderStatus; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/orders") public class AdminOrderController{
 private final AdminOrderService service;public AdminOrderController(AdminOrderService s){service=s;}
 @GetMapping public AdminOrderPageResponse list(@RequestParam(required=false)OrderStatus status,@RequestParam(required=false)Long customerId,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return service.list(status,customerId,page,size);}
 @GetMapping("/{id}") public AdminOrderDetailResponse get(@PathVariable Long id){return service.get(id);}}