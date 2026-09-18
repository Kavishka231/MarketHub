package com.markethub.wishlist;
import org.springframework.data.domain.Page; import java.util.List;
public record WishlistPageResponse(List<WishlistProductResponse> content,int page,int size,long totalElements,int totalPages){
 public static WishlistPageResponse from(Page<WishlistItem> items){return new WishlistPageResponse(items.map(WishlistProductResponse::from).getContent(),items.getNumber(),items.getSize(),items.getTotalElements(),items.getTotalPages());}
}