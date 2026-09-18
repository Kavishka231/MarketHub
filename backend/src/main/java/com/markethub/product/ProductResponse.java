package com.markethub.product;
import java.math.BigDecimal;
import java.time.Instant;
public record ProductResponse(Long id,String name,String description,BigDecimal price,int stockQuantity,String imageUrl,ProductStatus status,Long categoryId,String categoryName,Long vendorId,String vendorName,Instant createdAt,Instant updatedAt,double averageRating,long reviewCount){
 public static ProductResponse from(Product p){return from(p,0,0);}
 public static ProductResponse from(Product p,double average,long count){return new ProductResponse(p.getId(),p.getName(),p.getDescription(),p.getPrice(),p.getStockQuantity(),p.getImageUrl(),p.getStatus(),p.getCategory().getId(),p.getCategory().getName(),p.getVendor().getId(),p.getVendor().getStoreName(),p.getCreatedAt(),p.getUpdatedAt(),average,count);}
}