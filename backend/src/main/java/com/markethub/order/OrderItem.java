package com.markethub.order;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="order_items")
public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false) private Order order;
 @Column(name="product_id",nullable=false) private Long productId;
 @Column(name="vendor_id",nullable=false) private Long vendorId;
 @Column(name="product_name",nullable=false,length=200) private String productName;
 @Column(name="unit_price",nullable=false,precision=14,scale=2) private BigDecimal unitPrice;
 @Column(nullable=false) private int quantity;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal subtotal;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OrderStatus status;
 protected OrderItem() {}
 public OrderItem(Order o,Long p,Long v,String n,BigDecimal price,int q){order=o;productId=p;vendorId=v;productName=n;unitPrice=price;quantity=q;subtotal=price.multiply(BigDecimal.valueOf(q));status=OrderStatus.PENDING;}
 public Long getId(){return id;} public Order getOrder(){return order;} public Long getProductId(){return productId;} public Long getVendorId(){return vendorId;} public String getProductName(){return productName;} public BigDecimal getUnitPrice(){return unitPrice;} public int getQuantity(){return quantity;} public BigDecimal getSubtotal(){return subtotal;} public OrderStatus getStatus(){return status;} public void setStatus(OrderStatus s){status=s;}
}
