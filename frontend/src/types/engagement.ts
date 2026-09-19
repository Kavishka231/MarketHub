import type { ProductStatus } from "./marketplace";

export interface WishlistProduct {
  wishlistItemId: number;
  productId: number;
  productName: string;
  price: number;
  imageUrl: string | null;
  productStatus: ProductStatus;
  vendorId: number;
  vendorName: string;
  categoryId: number;
  categoryName: string;
  savedAt: string;
  available: boolean;
}

export interface WishlistPage {
  content: WishlistProduct[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type RatingValue = 1 | 2 | 3 | 4 | 5;

export interface ProductReview {
  id: number;
  customerName: string;
  rating: RatingValue;
  comment: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewPage {
  content: ProductReview[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  averageRating: number;
  reviewCount: number;
}

export interface ReviewRequest {
  rating: RatingValue;
  comment?: string;
}
