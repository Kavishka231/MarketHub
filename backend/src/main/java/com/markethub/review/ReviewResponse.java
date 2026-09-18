package com.markethub.review;
import java.time.Instant;
public record ReviewResponse(Long id,String customerName,int rating,String comment,Instant createdAt,Instant updatedAt){
 public static ReviewResponse from(Review r){String n=r.getCustomer().getFirstName()+" "+r.getCustomer().getLastName();return new ReviewResponse(r.getId(),n.trim(),r.getRating(),r.getComment(),r.getCreatedAt(),r.getUpdatedAt());}
}