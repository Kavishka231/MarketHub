import client from "./client";
import type {
  Category,
  PageResponse,
  Product,
  ProductQuery,
} from "../types/marketplace";

function backendParams(query: ProductQuery) {
  return {
    search: query.search,
    categoryId: query.categoryId,
    vendorId: query.vendorId,
    minPrice: query.minPrice,
    maxPrice: query.maxPrice,
    sort: query.sort ?? "newest",
    page: query.page ?? 0,
    size: query.size ?? 20,
  };
}

export async function fetchProducts(
  query: ProductQuery = {},
): Promise<PageResponse<Product>> {
  const response = await client.get<PageResponse<Product>>("/api/products", {
    params: backendParams(query),
  });
  return response.data;
}

export async function fetchProduct(productId: number): Promise<Product> {
  return (await client.get<Product>(`/api/products/${productId}`)).data;
}

export async function fetchCategories(): Promise<Category[]> {
  return (await client.get<Category[]>("/api/categories")).data;
}

/**
 * Retained for compatibility with earlier callers. New marketplace queries send
 * search text to the server so filtering happens before pagination.
 */
export function refineProductsBySearch(
  page: PageResponse<Product>,
  search?: string,
): PageResponse<Product> {
  const term = search?.trim().toLowerCase();
  if (!term) {
    return page;
  }

  return {
    ...page,
    content: page.content.filter((product) =>
      [
        product.name,
        product.description,
        product.vendorName,
        product.categoryName,
      ].some((value) => value?.toLowerCase().includes(term)),
    ),
  };
}
