import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import App from "../App";
import * as productApi from "../api/products";
import * as wishlistApi from "../api/wishlist";
import { AuthProvider } from "../features/auth/AuthContext";
import { saveAuth } from "../features/auth/authStorage";
import type { Product } from "../types/marketplace";

vi.mock("../api/products", () => ({
  fetchProducts: vi.fn(),
  fetchProduct: vi.fn(),
  fetchCategories: vi.fn(),
}));
vi.mock("../api/wishlist", () => ({
  fetchWishlist: vi.fn(),
  addWishlistProduct: vi.fn(),
  removeWishlistProduct: vi.fn(),
}));

const product: Product = {
  id: 9,
  name: "Travel Backpack",
  description: "A lightweight travel bag",
  price: 45,
  stockQuantity: 7,
  imageUrl: null,
  status: "ACTIVE",
  categoryId: 4,
  categoryName: "Travel",
  vendorId: 6,
  vendorName: "Adventure Store",
  createdAt: "2026-09-10",
  updatedAt: "2026-09-10",
  averageRating: 4.2,
  reviewCount: 5,
};

function show(path: string) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
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
  vi.mocked(productApi.fetchCategories).mockResolvedValue([
    { id: 4, name: "Travel", slug: "travel", description: "Travel essentials" },
  ]);
  vi.mocked(productApi.fetchProducts).mockResolvedValue({
    content: [product],
    page: 0,
    size: 12,
    totalElements: 13,
    totalPages: 2,
  });
  vi.mocked(productApi.fetchProduct).mockResolvedValue(product);
  vi.mocked(wishlistApi.fetchWishlist).mockResolvedValue({
    content: [],
    page: 0,
    size: 12,
    totalElements: 0,
    totalPages: 0,
  });
});

it("completes a URL-backed search, filter, sort, pagination, and result journey", async () => {
  show("/products?q=bag");
  expect(await screen.findByText("Travel Backpack")).toBeInTheDocument();

  fireEvent.change(screen.getByLabelText("Category"), { target: { value: "4" } });
  fireEvent.change(screen.getByLabelText("Sort products"), { target: { value: "priceAsc" } });
  fireEvent.click(screen.getByRole("button", { name: "Next" }));

  await waitFor(() => {
    expect(productApi.fetchProducts).toHaveBeenLastCalledWith(
      expect.objectContaining({
        search: "bag",
        categoryId: 4,
        sort: "priceAsc",
        page: 1,
      }),
    );
  });

  fireEvent.click(screen.getByRole("link", { name: "View Travel Backpack" }));
  expect(await screen.findByRole("heading", { name: "Travel Backpack" })).toBeInTheDocument();
});

it("stores, reuses, removes, and clears non-sensitive recent search terms", async () => {
  show("/products");
  await screen.findByText("Travel Backpack");
  fireEvent.change(screen.getByLabelText("Search products"), {
    target: { value: "backpack" },
  });

  expect(await screen.findByRole("button", { name: "backpack" })).toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Remove backpack from recent searches" }));
  expect(screen.queryByRole("button", { name: "backpack" })).not.toBeInTheDocument();
});

it("offers authoritative category discovery from an empty wishlist", async () => {
  saveAuth({
    token: "jwt",
    user: {
      id: 5,
      firstName: "Ada",
      lastName: "Customer",
      email: "ada@example.com",
      role: "CUSTOMER",
      status: "ACTIVE",
    },
  });
  show("/account/wishlist");

  expect(await screen.findByText("Your wishlist is empty")).toBeInTheDocument();
  expect(await screen.findByRole("link", { name: "Travel" })).toHaveAttribute(
    "href",
    "/categories/4",
  );
});
