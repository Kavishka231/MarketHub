package com.markethub.review;
import com.markethub.product.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class PublicReviewService {
 private final ReviewRepository reviews; private final ProductRepository products;
 public PublicReviewService(ReviewRepository r,ProductRepository p){reviews=r;products=p;}
 @Transactional(readOnly=true) public ReviewPageResponse list(Long productId,int page,int size){
  if(!products.existsById(productId))throw new ProductNotFoundException();
  Page<Review> result=reviews.findByProductId(productId,PageRequest.of(page,size,Sort.by("createdAt").descending()));
  long count=reviews.countByProductId(productId); double average=reviews.averageRatingByProductId(productId);
  return new ReviewPageResponse(result.map(ReviewResponse::from).getContent(),result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages(),average,count);
 }
}