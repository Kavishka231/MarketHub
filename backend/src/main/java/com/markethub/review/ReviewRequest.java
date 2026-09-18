package com.markethub.review;
import jakarta.validation.constraints.*;
public record ReviewRequest(
 @Min(value=1,message="Rating must be between 1 and 5") @Max(value=5,message="Rating must be between 1 and 5") int rating,
 @Size(max=2000,message="Comment must not exceed 2000 characters") String comment) {}