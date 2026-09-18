package com.markethub.admin;
import com.markethub.product.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/products") public class AdminProductController{
 private final AdminProductService service;public AdminProductController(AdminProductService s){service=s;}
 @GetMapping public AdminProductPageResponse list(@RequestParam(required=false)Long vendorId,@RequestParam(required=false)Long categoryId,@RequestParam(required=false)ProductStatus status,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return service.list(vendorId,categoryId,status,page,size);}
 @GetMapping("/{id}") public ProductResponse get(@PathVariable Long id){return service.get(id);}
 @PatchMapping("/{id}/deactivate") public ProductResponse deactivate(@PathVariable Long id){return service.deactivate(id);}}