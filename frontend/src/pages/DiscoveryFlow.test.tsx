import { fireEvent, render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import * as api from "../api/products";
import App from "../App";
import { AuthProvider } from "../features/auth/AuthContext";
import type { PageResponse, Product } from "../types/marketplace";

vi.mock("../api/products", () => ({
  fetchProducts: vi.fn(),
  fetchProduct: vi.fn(),
  fetchCategories: vi.fn(),
}));

const tea: Product = {
  id: 7,
  name: "Ceylon Tea",
  description: "Premium tea",
  price: 12,
  stockQuantity: 5,
  imageUrl: null,
  status: "ACTIVE",
  categoryId: 2,
  categoryName: "Groceries",
  vendorId: 3,
  vendorName: "Island Store",
  createdAt: "2026-09-01",
  updatedAt: "2026-09-01",
  averageRating: 4,
  reviewCount: 2,
};

const page = (content: Product[]): PageResponse<Product> => ({
  content,
  page: 0,
  size: 12,
  totalElements: content.length,
  totalPages: content.length ? 1 : 0,
});

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
  vi.mocked(api.fetchCategories).mockResolvedValue([
    { id: 2, name: "Groceries", slug: "groceries", description: "Food and drink" },
  ]);
  vi.mocked(api.fetchProducts).mockResolvedValue(page([tea]));
  vi.mocked(api.fetchProduct).mockResolvedValue(tea);
});

it("browses a category and opens its product", async () => {
  show("/categories/2");
  expect(await screen.findByRole("heading", { name: "Groceries" })).toBeInTheDocument();
  fireEvent.click(await screen.findByRole("link", { name: "View Ceylon Tea" }));
  expect(await screen.findByRole("heading", { name: "Ceylon Tea" })).toBeInTheDocument();
});

it("renders a public vendor-filtered storefront", async () => {
  show("/vendors/3");
  expect(await screen.findByRole("heading", { name: "Island Store" })).toBeInTheDocument();
  expect(api.fetchProducts).toHaveBeenCalledWith(expect.objectContaining({ vendorId: 3 }));
});

it("excludes the current product from related discovery", async () => {
  vi.mocked(api.fetchProducts).mockResolvedValue(page([tea, { ...tea, id: 8, name: "Green Tea" }]));
  show("/products/7");
  expect(await screen.findByRole("heading", { name: "More from this category" })).toBeInTheDocument();
  expect(await screen.findByText("Green Tea")).toBeInTheDocument();
});
