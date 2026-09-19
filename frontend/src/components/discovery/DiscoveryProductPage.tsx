import Pagination from "../marketplace/Pagination";
import ProductCardSkeleton from "../marketplace/ProductCardSkeleton";
import ProductGrid from "../marketplace/ProductGrid";
import { useProducts } from "../../hooks/useMarketplace";
import type { ProductQuery } from "../../types/marketplace";
import { apiErrorMessage } from "../../utils/apiError";

interface DiscoveryProductPageProps {
  title: string;
  description?: string | null;
  query: ProductQuery;
  emptyMessage: string;
  onPage: (page: number) => void;
}

export default function DiscoveryProductPage({
  title,
  description,
  query,
  emptyMessage,
  onPage,
}: DiscoveryProductPageProps) {
  const products = useProducts(query);

  return (
    <main className="mx-auto max-w-7xl px-6 py-10">
      <h1 className="text-3xl font-bold">{title}</h1>
      {description && <p className="mt-2 max-w-3xl text-slate-600">{description}</p>}

      <div className="mt-8">
        {products.isLoading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {Array.from({ length: 8 }, (_, index) => (
              <ProductCardSkeleton key={index} />
            ))}
          </div>
        ) : products.isError ? (
          <section className="rounded-xl border bg-white p-8 text-center">
            <p role="alert">{apiErrorMessage(products.error, "Products could not be loaded")}</p>
            <button className="mt-3 underline" onClick={() => products.refetch()} type="button">Try again</button>
          </section>
        ) : products.data?.content.length ? (
          <>
            <p className="mb-4 text-sm text-slate-500">{products.data.totalElements} products</p>
            <ProductGrid products={products.data.content} />
            <Pagination page={products.data.page} totalPages={products.data.totalPages} onPage={onPage} />
          </>
        ) : (
          <p className="rounded-xl border bg-white p-8 text-center text-slate-600">{emptyMessage}</p>
        )}
      </div>
    </main>
  );
}
