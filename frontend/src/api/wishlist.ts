import client from "./client";
import type { WishlistPage } from "../types/engagement";

export async function fetchWishlist(page = 0, size = 20): Promise<WishlistPage> {
  return (await client.get<WishlistPage>("/api/wishlist", { params: { page, size } })).data;
}

export async function addWishlistProduct(productId: number): Promise<void> {
  await client.post(`/api/wishlist/${productId}`);
}

export async function removeWishlistProduct(productId: number): Promise<void> {
  await client.delete(`/api/wishlist/${productId}`);
}
