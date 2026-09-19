import { useMemo } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import DiscoveryProductPage from "../components/discovery/DiscoveryProductPage";
import { useProducts } from "../hooks/useMarketplace";

export default function VendorStorefrontPage() {
  const vendorId = Number(useParams().vendorId);
  const [params, setParams] = useSearchParams();
  const page = Math.max(0, Number(params.get("page")) || 0);
  const identity = useProducts({ vendorId, page: 0, size: 1, sort: "newest" });
  const query = useMemo(
    () => ({ vendorId, page, size: 12, sort: "newest" as const }),
    [page, vendorId],
  );

  if (!Number.isInteger(vendorId) || vendorId < 1) {
    return <main className="mx-auto max-w-3xl px-6 py-20"><h1 className="text-3xl font-bold">Store not found</h1></main>;
  }
  if (identity.isLoading) {
    return <main className="mx-auto max-w-7xl px-6 py-10">Loading store…</main>;
  }
  const firstProduct = identity.data?.content[0];
  if (!firstProduct) {
    return <main className="mx-auto max-w-3xl px-6 py-20"><h1 className="text-3xl font-bold">Store not found</h1><p className="mt-2 text-slate-600">This store has no public products or is unavailable.</p></main>;
  }

  return (
    <DiscoveryProductPage
      description="Browse this store's currently public products."
      emptyMessage="This store has no public products."
      onPage={(next) => setParams(next ? { page: String(next) } : {})}
      query={query}
      title={firstProduct.vendorName}
    />
  );
}
