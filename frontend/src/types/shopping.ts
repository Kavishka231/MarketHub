export interface CartItem{itemId:number;productId:number;productName:string;imageUrl:string|null;unitPrice:number;quantity:number;lineTotal:number;stockQuantity:number;available:boolean}
export interface Cart{cartId:number;items:CartItem[];subtotal:number;totalItems:number}
export interface AddCartItemRequest{productId:number;quantity:number}
export interface UpdateCartItemRequest{quantity:number}
export interface Address{id:number;fullName:string;phone:string;addressLine1:string;addressLine2:string|null;city:string;district:string;postalCode:string|null;defaultAddress:boolean;createdAt:string;updatedAt:string}
export interface AddressRequest{fullName:string;phone:string;addressLine1:string;addressLine2?:string;city:string;district:string;postalCode?:string;defaultAddress:boolean}
export type PaymentMethod="CASH_ON_DELIVERY";export type OrderStatus="PENDING"|"CONFIRMED"|"PROCESSING"|"SHIPPED"|"DELIVERED"|"CANCELLED";
export interface OrderSummary{id:number;orderNumber:string;status:OrderStatus;subtotal:number;deliveryFee:number;total:number;paymentMethod:PaymentMethod;createdAt:string}
export interface OrderItem{id:number;productId:number;vendorId:number;productName:string;unitPrice:number;quantity:number;subtotal:number;status:OrderStatus}
export interface OrderDetail extends OrderSummary{deliveryFullName:string;deliveryPhone:string;deliveryAddressLine1:string;deliveryAddressLine2:string|null;deliveryCity:string;deliveryDistrict:string;deliveryPostalCode:string|null;items:OrderItem[]}
export interface OrderPage{content:OrderSummary[];page:number;size:number;totalElements:number;totalPages:number}
