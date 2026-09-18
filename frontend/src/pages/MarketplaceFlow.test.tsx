import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "../features/auth/AuthContext";
import App from "../App";
import * as api from "../api/products";
import type { PageResponse, Product } from "../types/marketplace";

vi.mock("../api/products", () => ({
  fetchProducts: vi.fn(), fetchProduct: vi.fn(), fetchCategories: vi.fn(),
  refineProductsBySearch: (page: PageResponse<Product>, search?: string) => {
    const term = search?.toLowerCase();
    return term ? { ...page, content: page.content.filter(product => product.name.toLowerCase().includes(term)) } : page;
  },
}));

const tea: Product = { id: 7, name: "Ceylon Tea", description: "Premium black tea", price: 12.5, stockQuantity: 8, imageUrl: null, status: "ACTIVE", categoryId: 2, categoryName: "Groceries", vendorId: 3, vendorName: "Island Store", createdAt: "2026-01-01", updatedAt: "2026-01-01", averageRating: 4.5, reviewCount: 10 };
const page = (content: Product[] = [tea], overrides: Partial<PageResponse<Product>> = {}): PageResponse<Product> => ({ content, page: 0, size: 12, totalElements: content.length, totalPages: 1, ...overrides });

function show(path = "/products") {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={[path]}><AuthProvider><App /></AuthProvider></MemoryRouter></QueryClientProvider>);
}

beforeEach(() => {
  localStorage.clear(); vi.resetAllMocks();
  vi.mocked(api.fetchCategories).mockResolvedValue([]);
  vi.mocked(api.fetchProducts).mockResolvedValue(page());
  vi.mocked(api.fetchProduct).mockResolvedValue(tea);
});

it("renders products returned by the API", async () => { show(); expect(await screen.findByText("Ceylon Tea")).toBeInTheDocument(); expect(screen.getByText("Island Store")).toBeInTheDocument(); });
it("renders product loading skeletons", () => { vi.mocked(api.fetchProducts).mockReturnValue(new Promise(() => {})); show(); expect(screen.getAllByLabelText("Loading product")).toHaveLength(8); });
it("renders an API error with retry", async () => { vi.mocked(api.fetchProducts).mockRejectedValue(new Error("Network unavailable")); show(); expect(await screen.findByRole("alert")).toHaveTextContent("Network unavailable"); expect(screen.getByRole("button", { name: "Try again" })).toBeInTheDocument(); });
it("renders an empty result state", async () => { vi.mocked(api.fetchProducts).mockResolvedValue(page([])); show(); expect(await screen.findByText("No products found")).toBeInTheDocument(); });
it("searching updates product query state", async () => { show(); await screen.findByText("Ceylon Tea"); fireEvent.change(screen.getByLabelText("Search products"), { target: { value: "tea" } }); fireEvent.click(screen.getByRole("button", { name: "Search" })); await waitFor(() => expect(api.fetchProducts).toHaveBeenLastCalledWith(expect.objectContaining({ search: "tea", page: 0 }))); });
it("category filtering updates the backend request", async () => { vi.mocked(api.fetchCategories).mockResolvedValue([{ id: 2, name: "Groceries", slug: "groceries", description: null }]); show(); await screen.findByText("Ceylon Tea"); fireEvent.change(screen.getByLabelText("Category"), { target: { value: "2" } }); await waitFor(() => expect(api.fetchProducts).toHaveBeenLastCalledWith(expect.objectContaining({ categoryId: 2, page: 0 }))); });
it("pagination requests the next backend page", async () => { vi.mocked(api.fetchProducts).mockResolvedValue(page([tea], { totalElements: 20, totalPages: 2 })); show(); await screen.findByText("Ceylon Tea"); fireEvent.click(screen.getByRole("button", { name: "Next" })); await waitFor(() => expect(api.fetchProducts).toHaveBeenLastCalledWith(expect.objectContaining({ page: 1 }))); });
it("restores filters from URL query parameters", async () => { show("/products?category=2&page=1&sort=priceDesc&minPrice=5"); await waitFor(() => expect(api.fetchProducts).toHaveBeenCalledWith(expect.objectContaining({ categoryId: 2, page: 1, sort: "priceDesc", minPrice: 5 }))); });
it("clicking a product navigates to its detail route", async () => { show(); fireEvent.click(await screen.findByRole("link", { name: "View Ceylon Tea" })); expect(await screen.findByRole("heading", { name: "Ceylon Tea" })).toBeInTheDocument(); expect(api.fetchProduct).toHaveBeenCalledWith(7); });
it("renders product detail data", async () => { show("/products/7"); expect(await screen.findByRole("heading", { name: "Ceylon Tea" })).toBeInTheDocument(); expect(screen.getByText("Premium black tea")).toBeInTheDocument(); expect(screen.getByText("Island Store")).toBeInTheDocument(); expect(screen.getByText("$12.50")).toBeInTheDocument(); });
it("shows a safe state for invalid or missing products", async () => { vi.mocked(api.fetchProduct).mockRejectedValue({ isAxiosError: true, response: { status: 404, data: { message: "Product not found" } } }); show("/products/999"); expect(await screen.findByRole("heading", { name: "This product could not be found" })).toBeInTheDocument(); });
