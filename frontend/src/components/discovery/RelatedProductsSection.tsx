import ProductGrid from "../marketplace/ProductGrid";
import { useProducts } from "../../hooks/useMarketplace";

interface RelatedProductsSectionProps {
  productId: number;
  categoryId: number;
}

export default function RelatedProductsSection({
  productId,
  categoryId,
}: RelatedProductsSectionProps) {
  const query = useProducts({ categoryId, page: 0, size: 5, sort: "newest" });
  const related = query.data?.content
    .filter((product) => product.id !== productId)
    .slice(0, 4);

  if (query.isError || (!query.isLoading && !related?.length)) {
    return null;
  }

  return (
    <section className="mt-14 border-t pt-10">
      <h2 className="text-2xl font-bold">More from this category</h2>
      <p className="mt-1 text-slate-600">
        Recently added public products in the same category.
      </p>
      {query.isLoading ? (
        <p className="mt-6">Loading related products…</p>
      ) : (
        <div className="mt-6">
          <ProductGrid products={related ?? []} />
        </div>
      )}
    </section>
  );
}
