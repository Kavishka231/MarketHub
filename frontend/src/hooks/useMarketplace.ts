import { keepPreviousData, useQuery } from "@tanstack/react-query";
import {
  fetchCategories,
  fetchProduct,
  fetchProducts,
} from "../api/products";
import type { ProductQuery } from "../types/marketplace";

export const marketplaceKeys = {
  all: ["marketplace"] as const,
  products: (query: ProductQuery) =>
    ["marketplace", "products", query] as const,
  product: (id: number) => ["marketplace", "product", id] as const,
  categories: ["marketplace", "categories"] as const,
};

export function useProducts(query: ProductQuery) {
  return useQuery({
    queryKey: marketplaceKeys.products(query),
    queryFn: () => fetchProducts(query),
    placeholderData: keepPreviousData,
    staleTime: 30 * 1000,
  });
}

export function useProduct(id: number) {
  return useQuery({
    queryKey: marketplaceKeys.product(id),
    queryFn: () => fetchProduct(id),
    enabled: Number.isInteger(id) && id > 0,
    staleTime: 60 * 1000,
    retry: (count, error) => {
      const status = (error as { response?: { status?: number } }).response
        ?.status;
      return status === 404 ? false : count < 2;
    },
  });
}

export function useCategories() {
  return useQuery({
    queryKey: marketplaceKeys.categories,
    queryFn: fetchCategories,
    staleTime: 5 * 60 * 1000,
  });
}
