import { Link } from "react-router-dom";
import ProductGrid from "../components/marketplace/ProductGrid";
import { useCategories, useProducts } from "../hooks/useMarketplace";

export default function HomePage() {
  const categories = useCategories();
  const newest = useProducts({ page: 0, size: 4, sort: "newest" });

  return (
    <main>
      <section className="bg-slate-900 px-6 py-20 text-center text-white">
        <p className="font-semibold text-brand-300">Multi-vendor marketplace</p>
        <h1 className="mt-3 text-5xl font-bold">Shop from trusted local vendors</h1>
        <p className="mx-auto mt-5 max-w-2xl text-lg text-slate-300">
          Search products from approved MarketHub stores and discover what was
          added most recently.
        </p>
        <Link className="mt-8 inline-block rounded bg-brand-600 px-6 py-3 font-semibold" to="/products">
          Explore all products
        </Link>
      </section>

      <section className="mx-auto max-w-7xl px-6 py-12">
        <h2 className="text-2xl font-bold">Browse categories</h2>
        {categories.isLoading ? (
          <p className="mt-4">Loading categories…</p>
        ) : categories.data?.length ? (
          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {categories.data.map((category) => (
              <Link className="rounded-xl border bg-white p-5 hover:border-brand-500" key={category.id} to={`/categories/${category.id}`}>
                <h3 className="font-bold">{category.name}</h3>
                {category.description && <p className="mt-2 text-sm text-slate-600">{category.description}</p>}
              </Link>
            ))}
          </div>
        ) : (
          <p className="mt-4 text-slate-600">No categories are available.</p>
        )}
      </section>

      <section className="mx-auto max-w-7xl px-6 pb-16">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold">New arrivals</h2>
            <p className="mt-1 text-slate-600">The newest public products on MarketHub.</p>
          </div>
          <Link className="underline" to="/products?sort=newest">View all</Link>
        </div>
        {newest.isLoading ? (
          <p className="mt-6">Loading new arrivals…</p>
        ) : newest.data?.content.length ? (
          <div className="mt-6"><ProductGrid products={newest.data.content} /></div>
        ) : (
          <p className="mt-6 rounded-xl border p-8 text-center">No products are available yet.</p>
        )}
      </section>
    </main>
  );
}
