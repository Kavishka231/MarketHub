package com.markethub.order;
import org.springframework.data.domain.Page; import java.util.List;
public record OrderPageResponse(List<OrderSummaryResponse> content,int page,int size,long totalElements,int totalPages){static OrderPageResponse from(Page<Order> p){return new OrderPageResponse(p.map(OrderSummaryResponse::from).getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());}}
