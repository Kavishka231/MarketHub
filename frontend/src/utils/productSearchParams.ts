import type { ProductQuery, ProductSort } from "../types/marketplace";

const sorts = new Set<ProductSort>(["newest", "priceAsc", "priceDesc"]);

function positiveNumber(value: string | null): number | undefined {
  if (!value) {
    return undefined;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : undefined;
}

export function parseProductSearchParams(params: URLSearchParams): ProductQuery {
  const rawSort = params.get("sort") as ProductSort | null;
  const page = positiveNumber(params.get("page"));
  return {
    search: params.get("q")?.trim().slice(0, 100) || undefined,
    categoryId: positiveNumber(params.get("category")),
    vendorId: positiveNumber(params.get("vendor")),
    minPrice: positiveNumber(params.get("minPrice")),
    maxPrice: positiveNumber(params.get("maxPrice")),
    sort: rawSort && sorts.has(rawSort) ? rawSort : "newest",
    page: page === undefined ? 0 : Math.floor(page),
    size: 12,
  };
}

export function updateProductSearchParams(
  current: URLSearchParams,
  updates: Record<string, string | number | undefined>,
  resetPage = true,
): URLSearchParams {
  const next = new URLSearchParams(current);
  Object.entries(updates).forEach(([key, value]) => {
    if (value === undefined || value === "") {
      next.delete(key);
    } else {
      next.set(key, String(value));
    }
  });
  if (resetPage) {
    next.delete("page");
  }
  return next;
}
