import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../features/auth/AuthContext";
import { useAddWishlistProduct, useRemoveWishlistProduct, useWishlist } from "../../hooks/useWishlist";
import { apiErrorMessage } from "../../utils/apiError";

export default function WishlistButton({ productId }: { productId: number }) {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const wishlist = useWishlist(0, 100, auth.user?.role === "CUSTOMER");
  const add = useAddWishlistProduct();
  const remove = useRemoveWishlistProduct();
  const saved = wishlist.data?.content.some((item) => item.productId === productId) ?? false;
  const mutation = saved ? remove : add;

  const toggle = () => {
    if (!auth.isAuthenticated || auth.user?.role !== "CUSTOMER") {
      navigate("/login", { state: { from: location.pathname } });
    } else if (!mutation.isPending) {
      mutation.mutate(productId);
    }
  };

  return (
    <div>
      <button
        aria-label={saved ? "Remove from wishlist" : "Save to wishlist"}
        className="rounded border border-brand-700 px-3 py-2 text-sm font-semibold text-brand-700 disabled:opacity-50"
        disabled={mutation.isPending || wishlist.isLoading}
        onClick={toggle}
        type="button"
      >
        {mutation.isPending ? "Saving…" : saved ? "Saved" : "Save"}
      </button>
      {mutation.isError && (
        <p className="mt-1 text-xs text-red-700" role="alert">
          {apiErrorMessage(mutation.error, "Wishlist could not be updated")}
        </p>
      )}
    </div>
  );
}
