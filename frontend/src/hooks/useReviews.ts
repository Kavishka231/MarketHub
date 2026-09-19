import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createProductReview, deleteProductReview, fetchProductReviews, updateProductReview } from "../api/reviews";
import type { ReviewRequest } from "../types/engagement";
import { marketplaceKeys } from "./useMarketplace";

export const reviewKeys = {
  all: ["reviews"] as const,
  product: (productId: number) => ["reviews", productId] as const,
  page: (productId: number, page: number) => ["reviews", productId, page] as const,
};

export function useProductReviews(productId: number, page: number) {
  return useQuery({
    queryKey: reviewKeys.page(productId, page),
    queryFn: () => fetchProductReviews(productId, page),
    enabled: productId > 0,
  });
}

function useReviewMutation(productId: number, action: (request: ReviewRequest) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: action,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: reviewKeys.product(productId) });
      await queryClient.invalidateQueries({ queryKey: marketplaceKeys.product(productId) });
      await queryClient.invalidateQueries({ queryKey: marketplaceKeys.all });
    },
  });
}

export function useCreateReview(productId: number) {
  return useReviewMutation(productId, (request) => createProductReview(productId, request));
}

export function useUpdateReview(productId: number) {
  return useReviewMutation(productId, (request) => updateProductReview(productId, request));
}

export function useDeleteReview(productId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => deleteProductReview(productId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: reviewKeys.product(productId) });
      await queryClient.invalidateQueries({ queryKey: marketplaceKeys.all });
    },
  });
}
