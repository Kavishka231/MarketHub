import client from "./client";import type{AddCartItemRequest,Cart,CartItem,UpdateCartItemRequest}from "../types/shopping";
export async function fetchCart():Promise<Cart>{return(await client.get<Cart>("/api/cart")).data}
export async function addCartItem(input:AddCartItemRequest):Promise<CartItem>{return(await client.post<CartItem>("/api/cart/items",input)).data}
export async function updateCartItem(itemId:number,input:UpdateCartItemRequest):Promise<CartItem>{return(await client.put<CartItem>(`/api/cart/items/${itemId}`,input)).data}
export async function removeCartItem(itemId:number):Promise<void>{await client.delete(`/api/cart/items/${itemId}`)}
export async function clearCart():Promise<void>{await client.delete("/api/cart")}
