package com.markethub.wishlist;
import com.markethub.product.Product; import com.markethub.user.User; import jakarta.persistence.*; import org.hibernate.annotations.CreationTimestamp; import java.time.Instant;
@Entity @Table(name="wishlist_items",uniqueConstraints=@UniqueConstraint(name="uk_wishlist_customer_product",columnNames={"customer_id","product_id"}))
public class WishlistItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private User customer;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id",nullable=false) private Product product;
 @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 protected WishlistItem(){}
 public WishlistItem(User customer,Product product){this.customer=customer;this.product=product;}
 public Long getId(){return id;} public User getCustomer(){return customer;} public Product getProduct(){return product;} public Instant getCreatedAt(){return createdAt;}
}