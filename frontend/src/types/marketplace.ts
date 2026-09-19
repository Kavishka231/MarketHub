export type ProductStatus =
  | "ACTIVE"
  | "INACTIVE"
  | "OUT_OF_STOCK"
  | "ARCHIVED";

export type ProductSort = "newest" | "priceAsc" | "priceDesc";

export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stockQuantity: number;
  imageUrl: string | null;
  status: ProductStatus;
  categoryId: number;
  categoryName: string;
  vendorId: number;
  vendorName: string;
  createdAt: string;
  updatedAt: string;
  averageRating: number;
  reviewCount: number;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string | null;
}

export interface VendorSummary {
  id: number;
  name: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ProductQuery {
  search?: string;
  categoryId?: number;
  vendorId?: number;
  minPrice?: number;
  maxPrice?: number;
  sort?: ProductSort;
  page?: number;
  size?: number;
}
