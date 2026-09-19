import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import * as reviewApi from "../../api/reviews";
import { AuthProvider } from "../../features/auth/AuthContext";
import { saveAuth } from "../../features/auth/authStorage";
import ReviewSection from "./ReviewSection";

vi.mock("../../api/reviews", () => ({
  fetchProductReviews: vi.fn(),
  createProductReview: vi.fn(),
  updateProductReview: vi.fn(),
  deleteProductReview: vi.fn(),
}));

function renderSection() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <AuthProvider>
          <ReviewSection productId={7} />
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

beforeEach(() => {
  localStorage.clear();
  vi.resetAllMocks();
  vi.mocked(reviewApi.fetchProductReviews).mockResolvedValue({
    content: [
      {
        id: 10,
        customerName: "Sam Customer",
        rating: 4,
        comment: "Very good tea",
        createdAt: "2026-09-01T10:00:00Z",
        updatedAt: "2026-09-01T10:00:00Z",
      },
    ],
    page: 0,
    size: 10,
    totalElements: 1,
    totalPages: 1,
    averageRating: 4,
    reviewCount: 1,
  });
});

it("renders the public rating summary and safe review fields", async () => {
  renderSection();

  expect(await screen.findByText("4.0 / 5 from 1 reviews")).toBeInTheDocument();
  expect(screen.getByText("Sam Customer")).toBeInTheDocument();
  expect(screen.getByText("Very good tea")).toBeInTheDocument();
  expect(screen.queryByRole("button", { name: "Save review" })).not.toBeInTheDocument();
});

it("allows a customer to submit a backend-verified review", async () => {
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
  vi.mocked(reviewApi.createProductReview).mockResolvedValue({
    id: 11,
    customerName: "Ada Customer",
    rating: 5,
    comment: "Excellent",
    createdAt: "2026-09-19T10:00:00Z",
    updatedAt: "2026-09-19T10:00:00Z",
  });
  renderSection();

  fireEvent.change(await screen.findByLabelText("Comment (optional)"), {
    target: { value: "Excellent" },
  });
  fireEvent.click(screen.getByRole("button", { name: "Save review" }));

  await waitFor(() => {
    expect(reviewApi.createProductReview).toHaveBeenCalledWith(7, {
      rating: 5,
      comment: "Excellent",
    });
  });
});

it("surfaces backend purchase eligibility errors", async () => {
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
  vi.mocked(reviewApi.createProductReview).mockRejectedValue(
    new Error("A delivered purchase is required to review this product"),
  );
  renderSection();

  fireEvent.click(await screen.findByRole("button", { name: "Save review" }));

  expect(await screen.findByRole("alert")).toHaveTextContent(
    "A delivered purchase is required to review this product",
  );
});
