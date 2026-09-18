package com.markethub.admin;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/dashboard") public class AdminDashboardController{private final AdminDashboardService service;public AdminDashboardController(AdminDashboardService s){service=s;}@GetMapping public AdminDashboardResponse get(){return service.get();}}