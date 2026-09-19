import { Link, useSearchParams } from "react-router-dom";
import Pagination from "../components/marketplace/Pagination";
import WishlistButton from "../components/wishlist/WishlistButton";
import { useWishlist } from "../hooks/useWishlist";
import { apiErrorMessage } from "../utils/apiError";

const money = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });

export default function WishlistPage() {
  const [params, setParams] = useSearchParams();
  const page = Math.max(0, Number(params.get("page")) || 0);
  const wishlist = useWishlist(page, 12);

  if (wishlist.isLoading) {
    return <main className="mx-auto max-w-6xl px-6 py-10">Loading your wishlist…</main>;
  }

  if (wishlist.isError) {
    return (
      <main className="mx-auto max-w-3xl px-6 py-16 text-center">
        <p role="alert">{apiErrorMessage(wishlist.error, "Could not load your wishlist")}</p>
        <button className="mt-4 underline" onClick={() => wishlist.refetch()} type="button">
          Try again
        </button>
      </main>
    );
  }

  const data = wishlist.data!;
  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="text-3xl font-bold">My wishlist</h1>
      <p className="mt-2 text-slate-600">Products you saved for later.</p>
      {data.content.length === 0 ? (
        <section className="mt-10 rounded-xl border bg-white p-10 text-center">
          <h2 className="text-xl font-semibold">Your wishlist is empty</h2>
          <Link className="mt-4 inline-block underline" to="/products">Browse products</Link>
        </section>
      ) : (
        <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {data.content.map((item) => (
            <article className="rounded-xl border bg-white p-5" key={item.wishlistItemId}>
              <p className="text-sm text-brand-700">{item.categoryName}</p>
              <Link className="text-xl font-bold" to={`/products/${item.productId}`}>
                {item.productName}
              </Link>
              <p className="text-sm text-slate-500">{item.vendorName}</p>
              <div className="my-4 flex justify-between">
                <strong>{money.format(item.price)}</strong>
                <span className={item.available ? "text-emerald-700" : "text-red-700"}>
                  {item.available ? "Available" : "Unavailable"}
                </span>
              </div>
              <WishlistButton productId={item.productId} />
            </article>
          ))}
        </div>
      )}
      <Pagination
        page={data.page}
        totalPages={data.totalPages}
        onPage={(next) => setParams(next ? { page: String(next) } : {})}
      />
    </main>
  );
}
