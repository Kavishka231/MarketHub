package com.markethub.product;
import org.springframework.data.domain.Page;
import java.util.*;
import java.util.function.Function;
public record ProductPageResponse(List<ProductResponse> content,int page,int size,long totalElements,int totalPages){
 public static ProductPageResponse from(Page<Product> products){return from(products,ProductResponse::from);}
 public static ProductPageResponse from(Page<Product> products,Function<Product,ProductResponse> mapper){return new ProductPageResponse(products.getContent().stream().map(mapper).toList(),products.getNumber(),products.getSize(),products.getTotalElements(),products.getTotalPages());}
}