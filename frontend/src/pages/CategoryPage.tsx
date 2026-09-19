import { useMemo } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import DiscoveryProductPage from "../components/discovery/DiscoveryProductPage";
import { useCategories } from "../hooks/useMarketplace";

export default function CategoryPage() {
  const categoryId = Number(useParams().categoryId);
  const [params, setParams] = useSearchParams();
  const categories = useCategories();
  const category = categories.data?.find((item) => item.id === categoryId);
  const page = Math.max(0, Number(params.get("page")) || 0);

  const query = useMemo(
    () => ({ categoryId, page, size: 12, sort: "newest" as const }),
    [categoryId, page],
  );

  if (!Number.isInteger(categoryId) || categoryId < 1) {
    return <main className="mx-auto max-w-3xl px-6 py-20"><h1 className="text-3xl font-bold">Category not found</h1></main>;
  }
  if (categories.isLoading) {
    return <main className="mx-auto max-w-7xl px-6 py-10">Loading category…</main>;
  }
  if (!category) {
    return <main className="mx-auto max-w-3xl px-6 py-20"><h1 className="text-3xl font-bold">Category not found</h1></main>;
  }

  return (
    <DiscoveryProductPage
      description={category.description}
      emptyMessage="No public products are available in this category yet."
      onPage={(next) => setParams(next ? { page: String(next) } : {})}
      query={query}
      title={category.name}
    />
  );
}
