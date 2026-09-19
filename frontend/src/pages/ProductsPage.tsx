import { useCallback, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import EmptyState from "../components/marketplace/EmptyState";
import ErrorState from "../components/marketplace/ErrorState";
import Pagination from "../components/marketplace/Pagination";
import ProductCardSkeleton from "../components/marketplace/ProductCardSkeleton";
import ProductFilters from "../components/marketplace/ProductFilters";
import ProductGrid from "../components/marketplace/ProductGrid";
import ProductSearch from "../components/marketplace/ProductSearch";
import ProductSort from "../components/marketplace/ProductSort";
import { useCategories, useProducts } from "../hooks/useMarketplace";
import type { ProductSort as Sort } from "../types/marketplace";
import { normalizeApiError } from "../utils/apiError";
import {
  parseProductSearchParams,
  updateProductSearchParams,
} from "../utils/productSearchParams";

export default function ProductsPage() {
  const [params, setParams] = useSearchParams();
  const query = useMemo(() => parseProductSearchParams(params), [params]);
  const products = useProducts(query);
  const categories = useCategories();

  const update = useCallback(
    (
      values: Record<string, string | number | undefined>,
      resetPage = true,
    ) => {
      setParams(updateProductSearchParams(params, values, resetPage));
    },
    [params, setParams],
  );

  const activeFilters = [
    query.search && { key: "q", label: `Search: ${query.search}` },
    query.categoryId && {
      key: "category",
      label:
        categories.data?.find((category) => category.id === query.categoryId)
          ?.name ?? `Category ${query.categoryId}`,
    },
    query.vendorId && {
      key: "vendor",
      label: `Store ${query.vendorId}`,
    },
    query.minPrice !== undefined && {
      key: "minPrice",
      label: `From $${query.minPrice}`,
    },
    query.maxPrice !== undefined && {
      key: "maxPrice",
      label: `Up to $${query.maxPrice}`,
    },
  ].filter(Boolean) as Array<{ key: string; label: string }>;

  const resetFilters = () => setParams({});

  return (
    <main className="mx-auto max-w-7xl px-6 py-10">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-3xl font-bold">Find products</h1>
          <p className="mt-2 text-slate-600">
            Search every public product from approved MarketHub vendors.
          </p>
        </div>
        <ProductSort
          onChange={(sort: Sort) => update({ sort })}
          value={query.sort!}
        />
      </div>

      <div className="mt-6 space-y-4">
        <ProductSearch
          onChange={(search) => update({ q: search || undefined })}
          value={query.search ?? ""}
        />
        <ProductFilters
          categories={categories.data ?? []}
          categoryId={query.categoryId}
          maxPrice={query.maxPrice}
          minPrice={query.minPrice}
          onCategory={(category) => update({ category })}
          onPrice={(minPrice, maxPrice) =>
            update({ minPrice, maxPrice })
          }
        />
        {activeFilters.length > 0 && (
          <div className="flex flex-wrap items-center gap-2" aria-label="Active filters">
            {activeFilters.map((filter) => (
              <button
                className="rounded-full bg-brand-50 px-3 py-1 text-sm text-brand-800"
                key={filter.key}
                onClick={() => update({ [filter.key]: undefined })}
                type="button"
              >
                {filter.label} ×
              </button>
            ))}
            <button className="text-sm underline" onClick={resetFilters} type="button">
              Reset all filters
            </button>
          </div>
        )}
      </div>

      <div className="mt-8">
        {products.isLoading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {Array.from({ length: 8 }, (_, index) => (
              <ProductCardSkeleton key={index} />
            ))}
          </div>
        ) : products.isError ? (
          <ErrorState
            message={normalizeApiError(products.error, "Please try again.").message}
            onRetry={() => products.refetch()}
          />
        ) : !products.data?.content.length ? (
          <section className="rounded-xl border bg-white p-10 text-center">
            <EmptyState />
            {activeFilters.length > 0 && (
              <button className="mt-4 underline" onClick={resetFilters} type="button">
                Clear filters and browse all products
              </button>
            )}
          </section>
        ) : (
          <>
            <p className="mb-4 text-sm text-slate-500">
              {products.data.totalElements} matching products
            </p>
            <ProductGrid products={products.data.content} />
            <Pagination
              onPage={(pageNumber) => update({ page: pageNumber }, false)}
              page={products.data.page}
              totalPages={products.data.totalPages}
            />
          </>
        )}
      </div>
    </main>
  );
}
