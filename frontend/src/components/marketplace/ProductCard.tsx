import { Link } from "react-router-dom";
import WishlistButton from "../wishlist/WishlistButton";
import type { Product } from "../../types/marketplace";

const money = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

export default function ProductCard({ product }: { product: Product }) {
  const available = product.status === "ACTIVE" && product.stockQuantity > 0;

  return (
    <article className="overflow-hidden rounded-xl border bg-white shadow-sm">
      <Link aria-label={`View ${product.name}`} to={`/products/${product.id}`}>
        <div className="aspect-[4/3] bg-slate-100">
          {product.imageUrl ? (
            <img
              alt={product.name}
              className="h-full w-full object-cover"
              src={product.imageUrl}
            />
          ) : (
            <div className="flex h-full items-center justify-center text-slate-400">
              No image
            </div>
          )}
        </div>
        <div className="p-4 pb-2">
          <p className="text-xs font-medium text-brand-600">{product.categoryName}</p>
          <h2 className="mt-1 text-lg font-semibold">{product.name}</h2>
          <p className="mt-1 text-sm text-slate-500">{product.vendorName}</p>
          {product.description && (
            <p className="mt-2 line-clamp-2 text-sm text-slate-600">
              {product.description}
            </p>
          )}
          <div className="mt-4 flex items-center justify-between">
            <span className="font-bold">{money.format(product.price)}</span>
            <span className={available ? "text-sm text-emerald-700" : "text-sm text-red-600"}>
              {available ? "In stock" : "Unavailable"}
            </span>
          </div>
        </div>
      </Link>
      <div className="px-4 pb-4">
        <WishlistButton productId={product.id} />
      </div>
    </article>
  );
}
