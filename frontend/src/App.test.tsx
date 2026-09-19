import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import App from "./App";
import * as productApi from "./api/products";
import { AuthProvider } from "./features/auth/AuthContext";

vi.mock("./api/products", () => ({
  fetchProducts: vi.fn(),
  fetchProduct: vi.fn(),
  fetchCategories: vi.fn(),
}));

describe("application routes", () => {
  it("renders the home discovery route", async () => {
    vi.mocked(productApi.fetchCategories).mockResolvedValue([]);
    vi.mocked(productApi.fetchProducts).mockResolvedValue({
      content: [],
      page: 0,
      size: 4,
      totalElements: 0,
      totalPages: 0,
    });
    const client = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });

    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <AuthProvider>
            <App />
          </AuthProvider>
        </MemoryRouter>
      </QueryClientProvider>,
    );

    expect(
      screen.getByRole("heading", { name: /shop from trusted/i }),
    ).toBeInTheDocument();
    expect(await screen.findByText("No categories are available.")).toBeInTheDocument();
  });
});
