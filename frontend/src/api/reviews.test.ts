import client from "./client";
import { createProductReview, deleteProductReview, fetchProductReviews, updateProductReview } from "./reviews";

vi.mock("./client", () => ({ default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() } }));

it("uses public and customer-owned review endpoints", async () => {
  vi.mocked(client.get).mockResolvedValue({ data: { content: [] } });
  vi.mocked(client.post).mockResolvedValue({ data: { id: 1 } });
  vi.mocked(client.put).mockResolvedValue({ data: { id: 1 } });
  await fetchProductReviews(7, 2, 10);
  expect(client.get).toHaveBeenCalledWith("/api/products/7/reviews", { params: { page: 2, size: 10 } });
  await createProductReview(7, { rating: 5, comment: "Excellent" });
  await updateProductReview(7, { rating: 4 });
  await deleteProductReview(7);
  expect(client.delete).toHaveBeenCalledWith("/api/products/7/reviews/me");
});
