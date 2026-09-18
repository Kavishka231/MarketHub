import { useState } from "react";
import { useAuth } from "../../features/auth/AuthContext";
import type { Product } from "../../types/marketplace";
export default function PurchasePanel({ product }: { product: Product }) {
  const { user } = useAuth(); const [quantity, setQuantity] = useState(1); const available = product.status === "ACTIVE" && product.stockQuantity > 0;
  return <aside className="mt-8 rounded-xl border bg-white p-4"><label className="text-sm font-medium">Quantity<input aria-label="Quantity" className="ml-3 w-20 rounded border p-2" min="1" max={Math.max(product.stockQuantity, 1)} type="number" value={quantity} onChange={event => setQuantity(Math.max(1, Number(event.target.value)))}/></label><button className="mt-4 w-full rounded bg-slate-300 px-4 py-3 font-semibold text-slate-600" disabled>{available ? "Cart interface coming soon" : "Product unavailable"}</button>{!user && <p className="mt-3 text-sm text-slate-500">Sign in as a customer when cart features become available.</p>}</aside>;
}
