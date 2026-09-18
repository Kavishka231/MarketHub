import{useMutation,useQuery,useQueryClient}from "@tanstack/react-query";import{addCartItem,clearCart,fetchCart,removeCartItem,updateCartItem}from "../api/cart";import type{AddCartItemRequest,UpdateCartItemRequest}from "../types/shopping";
export const cartKey=["customer","cart"]as const;export function useCart(){return useQuery({queryKey:cartKey,queryFn:fetchCart})}
export function useAddCartItem(){const c=useQueryClient();return useMutation({mutationFn:(input:AddCartItemRequest)=>addCartItem(input),onSuccess:()=>c.invalidateQueries({queryKey:cartKey})})}
export function useUpdateCartItem(){const c=useQueryClient();return useMutation({mutationFn:({itemId,input}:{itemId:number;input:UpdateCartItemRequest})=>updateCartItem(itemId,input),onSuccess:()=>c.invalidateQueries({queryKey:cartKey})})}
export function useRemoveCartItem(){const c=useQueryClient();return useMutation({mutationFn:removeCartItem,onSuccess:()=>c.invalidateQueries({queryKey:cartKey})})}
export function useClearCart(){const c=useQueryClient();return useMutation({mutationFn:clearCart,onSuccess:()=>c.invalidateQueries({queryKey:cartKey})})}
