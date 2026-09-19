import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { addWishlistProduct, fetchWishlist, removeWishlistProduct } from "../api/wishlist";
import { marketplaceKeys } from "./useMarketplace";

export const wishlistKeys = {
  all: ["wishlist"] as const,
  page: (page: number, size: number) => ["wishlist", page, size] as const,
};

export function useWishlist(page = 0, size = 20, enabled = true) {
  return useQuery({
    queryKey: wishlistKeys.page(page, size),
    queryFn: () => fetchWishlist(page, size),
    enabled,
  });
}

function useWishlistMutation(action: (productId: number) => Promise<void>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: action,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: wishlistKeys.all });
      await queryClient.invalidateQueries({ queryKey: marketplaceKeys.all });
    },
  });
}

export const useAddWishlistProduct = () => useWishlistMutation(addWishlistProduct);
export const useRemoveWishlistProduct = () => useWishlistMutation(removeWishlistProduct);
