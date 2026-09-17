package com.markethub.order;
import com.markethub.vendor.Vendor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service public class VendorOrderDetailService {private final VendorOrderService vendorService;private final VendorOrderRepository orders;private final VendorOrderItemRepository items;public VendorOrderDetailService(VendorOrderService v,VendorOrderRepository o,VendorOrderItemRepository i){vendorService=v;orders=o;items=i;}
 @Transactional(readOnly=true) public VendorOrderDetailResponse get(String email,Long id){Vendor v=vendorService.vendor(email);Order o=orders.findById(id).orElseThrow(OrderNotFoundException::new);List<OrderItem> own=items.findByOrderIdAndVendorIdOrderByIdAsc(id,v.getId());if(own.isEmpty())throw new OrderNotFoundException();return VendorOrderDetailResponse.from(o,own);}
}
