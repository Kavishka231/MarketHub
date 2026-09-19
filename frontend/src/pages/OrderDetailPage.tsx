import { Link, useParams } from "react-router-dom";
import OrderStatusBadge from "../components/orders/OrderStatusBadge";
import { useCancelOrder, useOrder } from "../hooks/useOrders";
import { apiErrorMessage, normalizeApiError } from "../utils/apiError";

const money = (value: number) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(value);

export default function OrderDetailPage() {
  const id = Number(useParams().orderId);
  const query = useOrder(id);
  const cancel = useCancelOrder(id);

  if (!Number.isInteger(id) || id < 1) {
    return <Missing />;
  }
  if (query.isLoading) {
    return <main className="mx-auto max-w-5xl px-6 py-10">Loading order…</main>;
  }
  if (query.isError) {
    const error = normalizeApiError(query.error);
    return error.status === 403 || error.status === 404 ? (
      <Missing />
    ) : (
      <main className="mx-auto max-w-3xl px-6 py-16">
        <p role="alert">{error.message}</p>
      </main>
    );
  }

  const order = query.data!;
  const cancellable = order.status === "PENDING" || order.status === "CONFIRMED";

  return (
    <main className="mx-auto max-w-5xl px-6 py-10">
      <Link className="text-sm underline" to="/account/orders">← All orders</Link>
      <div className="mt-5 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-bold">Order {order.orderNumber}</h1>
          <p className="text-slate-500">Placed {new Date(order.createdAt).toLocaleString()}</p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <div className="mt-8 grid gap-6 lg:grid-cols-[1fr_320px]">
        <section className="rounded-xl border bg-white p-5">
          <h2 className="text-xl font-bold">Items</h2>
          {order.items.map((item) => (
            <div className="flex justify-between border-b py-4" key={item.id}>
              <div>
                <Link className="font-semibold" to={`/products/${item.productId}`}>
                  {item.productName}
                </Link>
                <p className="text-sm text-slate-500">
                  {money(item.unitPrice)} × {item.quantity} · {item.status}
                </p>
                {item.status === "DELIVERED" && (
                  <Link
                    className="mt-2 inline-block text-sm font-semibold text-brand-700 underline"
                    to={`/products/${item.productId}#reviews`}
                  >
                    Review product
                  </Link>
                )}
              </div>
              <strong>{money(item.subtotal)}</strong>
            </div>
          ))}
        </section>

        <aside className="space-y-5">
          <section className="rounded-xl border bg-white p-5">
            <h2 className="font-bold">Delivery address</h2>
            <p className="mt-2 text-sm">
              {order.deliveryFullName}<br />
              {order.deliveryAddressLine1}
              {order.deliveryAddressLine2 && <><br />{order.deliveryAddressLine2}</>}
              <br />{order.deliveryCity}, {order.deliveryDistrict} {order.deliveryPostalCode}
              <br />{order.deliveryPhone}
            </p>
          </section>
          <section className="rounded-xl border bg-white p-5">
            <h2 className="font-bold">Totals</h2>
            <p className="mt-3 flex justify-between"><span>Subtotal</span><span>{money(order.subtotal)}</span></p>
            <p className="flex justify-between"><span>Delivery</span><span>{money(order.deliveryFee)}</span></p>
            <p className="mt-2 flex justify-between border-t pt-2 font-bold"><span>Total</span><span>{money(order.total)}</span></p>
            <p className="mt-2 text-xs text-slate-500">Cash on delivery</p>
          </section>
          {cancellable && (
            <button
              className="w-full rounded border border-red-600 px-4 py-2 text-red-700"
              disabled={cancel.isPending}
              onClick={() => window.confirm("Cancel this order?") && cancel.mutate()}
            >
              {cancel.isPending ? "Cancelling…" : "Cancel order"}
            </button>
          )}
          {cancel.isError && <p className="text-sm text-red-700" role="alert">{apiErrorMessage(cancel.error, "Could not cancel order")}</p>}
        </aside>
      </div>
    </main>
  );
}

function Missing() {
  return (
    <main className="mx-auto max-w-xl px-6 py-20 text-center">
      <h1 className="text-3xl font-bold">Order not found</h1>
      <p className="mt-2 text-slate-600">This order does not exist or is not available to your account.</p>
      <Link className="mt-4 inline-block underline" to="/account/orders">Back to orders</Link>
    </main>
  );
}
