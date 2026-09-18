import type { Product } from "../../types/marketplace";
const money = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
export default function ProductInformation({ product }: { product: Product }) {
  const available = product.status === "ACTIVE" && product.stockQuantity > 0;
  return <section><p className="text-sm font-semibold text-brand-600">{product.categoryName}</p><h1 className="mt-2 text-4xl font-bold">{product.name}</h1><p className="mt-3 text-slate-600">Sold by <span className="font-medium text-slate-900">{product.vendorName}</span></p><p className="mt-6 text-3xl font-bold">{money.format(product.price)}</p><div className="mt-3 flex gap-4 text-sm"><span className={available ? "text-emerald-700" : "text-red-600"}>{available ? product.stockQuantity + " available" : "Currently unavailable"}</span>{product.reviewCount > 0 && <span>{product.averageRating.toFixed(1)} / 5 ({product.reviewCount} reviews)</span>}</div>{product.description && <div className="mt-8"><h2 className="text-lg font-semibold">Description</h2><p className="mt-2 whitespace-pre-line text-slate-600">{product.description}</p></div>}</section>;
}
