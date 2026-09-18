package com.markethub.product;
import com.markethub.review.ReviewRepository;
import com.markethub.vendor.VendorStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
@Service
public class PublicProductService {
 private final PublicProductRepository products; private final ReviewRepository reviews;
 public PublicProductService(PublicProductRepository p,ReviewRepository r){products=p;reviews=r;}
 @Transactional(readOnly=true) public ProductPageResponse list(Long categoryId,Long vendorId,BigDecimal minPrice,BigDecimal maxPrice,String sort,int page,int size){
  Specification<Product> spec=visible();
  if(categoryId!=null)spec=spec.and((r,q,c)->c.equal(r.get("category").get("id"),categoryId));
  if(vendorId!=null)spec=spec.and((r,q,c)->c.equal(r.get("vendor").get("id"),vendorId));
  if(minPrice!=null)spec=spec.and((r,q,c)->c.greaterThanOrEqualTo(r.get("price"),minPrice));
  if(maxPrice!=null)spec=spec.and((r,q,c)->c.lessThanOrEqualTo(r.get("price"),maxPrice));
  Sort ordering=switch(sort){case "priceAsc"->Sort.by("price").ascending();case "priceDesc"->Sort.by("price").descending();default->Sort.by("createdAt").descending();};
  return ProductPageResponse.from(products.findAll(spec,PageRequest.of(page,size,ordering)),this::withRating);
 }
 @Transactional(readOnly=true) public ProductResponse get(Long id){Product p=products.findOne(visible().and((r,q,c)->c.equal(r.get("id"),id))).orElseThrow(ProductNotFoundException::new);return withRating(p);}
 private ProductResponse withRating(Product p){long count=reviews.countByProductId(p.getId());return ProductResponse.from(p,count==0?0:reviews.averageRatingByProductId(p.getId()),count);}
 private Specification<Product> visible(){return (r,q,c)->c.and(c.equal(r.get("status"),ProductStatus.ACTIVE),c.equal(r.get("vendor").get("status"),VendorStatus.APPROVED),c.isTrue(r.get("category").get("active")));}
}