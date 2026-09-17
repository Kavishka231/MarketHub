package com.markethub.order;

import com.markethub.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="orders")
public class Order {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private User customer;
 @Column(name="order_number",nullable=false,unique=true,length=40) private String orderNumber;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OrderStatus status;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal subtotal;
 @Column(name="delivery_fee",nullable=false,precision=14,scale=2) private BigDecimal deliveryFee;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal total;
 @Enumerated(EnumType.STRING) @Column(name="payment_method",nullable=false,length=30) private PaymentMethod paymentMethod;
 @Column(name="delivery_full_name",nullable=false,length=150) private String deliveryFullName;
 @Column(name="delivery_phone",nullable=false,length=30) private String deliveryPhone;
 @Column(name="delivery_address_line1",nullable=false,length=255) private String deliveryAddressLine1;
 @Column(name="delivery_address_line2",length=255) private String deliveryAddressLine2;
 @Column(name="delivery_city",nullable=false,length=100) private String deliveryCity;
 @Column(name="delivery_district",nullable=false,length=100) private String deliveryDistrict;
 @Column(name="delivery_postal_code",length=20) private String deliveryPostalCode;
 @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected Order() {}
 public Order(User c,String n,BigDecimal s,BigDecimal f,PaymentMethod p,String fn,String ph,String a1,String a2,String city,String district,String pc){customer=c;orderNumber=n;status=OrderStatus.PENDING;subtotal=s;deliveryFee=f;total=s.add(f);paymentMethod=p;deliveryFullName=fn;deliveryPhone=ph;deliveryAddressLine1=a1;deliveryAddressLine2=a2;deliveryCity=city;deliveryDistrict=district;deliveryPostalCode=pc;}
 public Long getId(){return id;} public User getCustomer(){return customer;} public String getOrderNumber(){return orderNumber;} public OrderStatus getStatus(){return status;} public void setStatus(OrderStatus s){status=s;} public BigDecimal getSubtotal(){return subtotal;} public BigDecimal getDeliveryFee(){return deliveryFee;} public BigDecimal getTotal(){return total;} public PaymentMethod getPaymentMethod(){return paymentMethod;} public String getDeliveryFullName(){return deliveryFullName;} public String getDeliveryPhone(){return deliveryPhone;} public String getDeliveryAddressLine1(){return deliveryAddressLine1;} public String getDeliveryAddressLine2(){return deliveryAddressLine2;} public String getDeliveryCity(){return deliveryCity;} public String getDeliveryDistrict(){return deliveryDistrict;} public String getDeliveryPostalCode(){return deliveryPostalCode;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
