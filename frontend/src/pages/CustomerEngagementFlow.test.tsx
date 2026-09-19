import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import * as ordersApi from "../api/orders";
import * as wishlistApi from "../api/wishlist";
import { AuthProvider } from "../features/auth/AuthContext";
import { saveAuth } from "../features/auth/authStorage";
import App from "../App";

vi.mock("../api/orders", () => ({ fetchOrders: vi.fn(), fetchOrder: vi.fn(), cancelOrder: vi.fn() }));
vi.mock("../api/wishlist", () => ({
  fetchWishlist: vi.fn(),
  addWishlistProduct: vi.fn(),
  removeWishlistProduct: vi.fn(),
}));

function show(path: string) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <AuthProvider><App /></AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

beforeEach(() => {
  localStorage.clear();
  vi.resetAllMocks();
  saveAuth({
    token: "jwt",
    user: { id: 5, firstName: "Ada", lastName: "Customer", email: "ada@example.com", role: "CUSTOMER", status: "ACTIVE" },
  });
  vi.mocked(ordersApi.fetchOrders).mockResolvedValue({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  vi.mocked(wishlistApi.fetchWishlist).mockResolvedValue({
    content: [{ wishlistItemId: 1, productId: 7, productName: "Ceylon Tea", price: 10, imageUrl: null, productStatus: "ACTIVE", vendorId: 2, vendorName: "Island Store", categoryId: 3, categoryName: "Tea", savedAt: "2026-09-19", available: true }],
    page: 0, size: 4, totalElements: 1, totalPages: 1,
  });
});

it("shows authoritative saved-product and recent-order account information", async () => {
  show("/account");
  expect(await screen.findByRole("heading", { name: "My account" })).toBeInTheDocument();
  expect(await screen.findByText("Ceylon Tea")).toBeInTheDocument();
  expect(screen.getByText("No orders yet.")).toBeInTheDocument();
});

it("protects engagement account routes", async () => {
  localStorage.clear();
  show("/account/wishlist");
  expect(await screen.findByRole("heading", { name: "Sign in" })).toBeInTheDocument();
});
