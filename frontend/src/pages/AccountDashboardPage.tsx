import { Link } from "react-router-dom";
import { useOrders } from "../hooks/useOrders";
import { useWishlist } from "../hooks/useWishlist";
import { apiErrorMessage } from "../utils/apiError";

export default function AccountDashboardPage() {
  const orders = useOrders(0);
  const wishlist = useWishlist(0, 4);

  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="text-3xl font-bold">My account</h1>
      <p className="mt-2 text-slate-600">Your recent shopping and saved products.</p>

      <div className="mt-8 grid gap-6 lg:grid-cols-2">
        <section className="rounded-xl border bg-white p-6">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold">Recent orders</h2>
            <Link className="text-sm underline" to="/account/orders">View all</Link>
          </div>
          {orders.isError ? (
            <p className="mt-4 text-red-700" role="alert">{apiErrorMessage(orders.error, "Could not load orders")}</p>
          ) : orders.isLoading ? (
            <p className="mt-4">Loading orders…</p>
          ) : orders.data?.content.length ? (
            <div className="mt-4 space-y-3">
              {orders.data.content.slice(0, 3).map((order) => (
                <Link className="flex justify-between rounded-lg bg-slate-50 p-3" key={order.id} to={`/account/orders/${order.id}`}>
                  <span>{order.orderNumber}</span>
                  <strong>{order.status}</strong>
                </Link>
              ))}
            </div>
          ) : (
            <p className="mt-4 text-slate-600">No orders yet.</p>
          )}
        </section>

        <section className="rounded-xl border bg-white p-6">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold">Saved products</h2>
            <Link className="text-sm underline" to="/account/wishlist">View wishlist</Link>
          </div>
          {wishlist.isError ? (
            <p className="mt-4 text-red-700" role="alert">{apiErrorMessage(wishlist.error, "Could not load wishlist")}</p>
          ) : wishlist.isLoading ? (
            <p className="mt-4">Loading wishlist…</p>
          ) : wishlist.data?.content.length ? (
            <div className="mt-4 space-y-3">
              {wishlist.data.content.map((item) => (
                <Link className="flex justify-between rounded-lg bg-slate-50 p-3" key={item.wishlistItemId} to={`/products/${item.productId}`}>
                  <span>{item.productName}</span>
                  <span className={item.available ? "text-emerald-700" : "text-red-700"}>{item.available ? "Available" : "Unavailable"}</span>
                </Link>
              ))}
            </div>
          ) : (
            <p className="mt-4 text-slate-600">No saved products yet.</p>
          )}
        </section>
      </div>
    </main>
  );
}
