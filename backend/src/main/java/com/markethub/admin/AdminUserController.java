package com.markethub.admin;
import com.markethub.user.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/users") public class AdminUserController{
 private final AdminUserService service;public AdminUserController(AdminUserService s){service=s;}
 @GetMapping public AdminUserPageResponse list(@RequestParam(required=false)UserRole role,@RequestParam(required=false)UserStatus status,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return service.list(role,status,page,size);}
 @GetMapping("/{id}") public AdminUserResponse get(@PathVariable Long id){return service.get(id);}
 @PatchMapping("/{id}/disable") public AdminUserResponse disable(Authentication a,@PathVariable Long id){return service.disable(a.getName(),id);}
 @PatchMapping("/{id}/enable") public AdminUserResponse enable(@PathVariable Long id){return service.enable(id);}}