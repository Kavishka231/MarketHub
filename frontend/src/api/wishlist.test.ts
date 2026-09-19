import client from "./client";
import { addWishlistProduct, fetchWishlist, removeWishlistProduct } from "./wishlist";

vi.mock("./client", () => ({
  default: { get: vi.fn(), post: vi.fn(), delete: vi.fn() },
}));

it("uses the real wishlist endpoints", async () => {
  vi.mocked(client.get).mockResolvedValue({ data: { content: [], page: 2 } });
  await fetchWishlist(2, 12);
  expect(client.get).toHaveBeenCalledWith("/api/wishlist", { params: { page: 2, size: 12 } });

  await addWishlistProduct(7);
  expect(client.post).toHaveBeenCalledWith("/api/wishlist/7");

  await removeWishlistProduct(7);
  expect(client.delete).toHaveBeenCalledWith("/api/wishlist/7");
});
