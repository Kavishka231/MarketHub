import client from "./client";
import type { ProductReview, ReviewPage, ReviewRequest } from "../types/engagement";

export async function fetchProductReviews(productId: number, page = 0, size = 10): Promise<ReviewPage> {
  return (await client.get<ReviewPage>(`/api/products/${productId}/reviews`, { params: { page, size } })).data;
}

export async function createProductReview(productId: number, request: ReviewRequest): Promise<ProductReview> {
  return (await client.post<ProductReview>(`/api/products/${productId}/reviews`, request)).data;
}

export async function updateProductReview(productId: number, request: ReviewRequest): Promise<ProductReview> {
  return (await client.put<ProductReview>(`/api/products/${productId}/reviews/me`, request)).data;
}

export async function deleteProductReview(productId: number): Promise<void> {
  await client.delete(`/api/products/${productId}/reviews/me`);
}
