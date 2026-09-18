import { Link, useParams } from "react-router-dom";
import { useProduct } from "../hooks/useMarketplace";
import ProductGallery from "../components/marketplace/ProductGallery";
import ProductInformation from "../components/marketplace/ProductInformation";
import PurchasePanel from "../components/marketplace/PurchasePanel";
import ErrorState from "../components/marketplace/ErrorState";
import { normalizeApiError } from "../utils/apiError";
export default function ProductDetailPage() {
  const id = Number(useParams().productId); const query = useProduct(id);
  if (!Number.isInteger(id) || id < 1) return <NotFound />;
  if (query.isLoading) return <main className="mx-auto max-w-6xl animate-pulse px-6 py-10"><div className="h-5 w-32 rounded bg-slate-200"/><div className="mt-8 grid gap-10 md:grid-cols-2"><div className="aspect-square rounded-2xl bg-slate-200"/><div className="space-y-5"><div className="h-10 rounded bg-slate-200"/><div className="h-6 w-1/2 rounded bg-slate-200"/></div></div></main>;
  if (query.isError) { const error = normalizeApiError(query.error); return error.status === 404 ? <NotFound /> : <main className="mx-auto max-w-4xl px-6 py-16"><ErrorState message={error.message} onRetry={() => query.refetch()} /></main>; }
  const product = query.data!;
  return <main className="mx-auto max-w-6xl px-6 py-10"><nav className="text-sm text-slate-500"><Link className="hover:text-brand-700" to="/products">Products</Link><span> / {product.name}</span></nav><div className="mt-8 grid gap-10 md:grid-cols-2"><ProductGallery imageUrl={product.imageUrl} name={product.name}/><div><ProductInformation product={product}/><PurchasePanel product={product}/></div></div></main>;
}
function NotFound() { return <main className="mx-auto max-w-xl px-6 py-20 text-center"><p className="text-sm font-semibold text-brand-600">Product unavailable</p><h1 className="mt-2 text-3xl font-bold">This product could not be found</h1><p className="mt-3 text-slate-600">It may have been removed or is no longer publicly available.</p><Link className="mt-6 inline-block text-brand-700 underline" to="/products">Back to products</Link></main>; }
