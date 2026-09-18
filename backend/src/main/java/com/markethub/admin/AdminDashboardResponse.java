package com.markethub.admin;
import java.math.BigDecimal;
public record AdminDashboardResponse(long totalUsers,long totalCustomers,long totalVendors,long activeVendors,long pendingVendors,long suspendedVendors,long totalProducts,long activeProducts,long outOfStockProducts,long totalOrders,long pendingOrders,long deliveredOrders,BigDecimal totalSales){}