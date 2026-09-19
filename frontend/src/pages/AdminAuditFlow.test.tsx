import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import * as auditApi from "../api/audit";
import App from "../App";
import { AuthProvider } from "../features/auth/AuthContext";
import { saveAuth } from "../features/auth/authStorage";

vi.mock("../api/audit", () => ({
  fetchAuditLogs: vi.fn(),
  fetchAuditLog: vi.fn(),
}));

const audit = {
  id: 4,
  occurredAt: "2026-09-19T12:00:00Z",
  actorUserId: 1,
  actorEmail: "admin@example.com",
  actorRole: "ADMIN",
  action: "ADMIN_USER_DISABLE",
  resourceType: "USER",
  resourceId: "7",
  requestId: "request-42",
  result: "SUCCESS" as const,
  metadata: '{"operation":"ADMIN_USER_DISABLE"}',
};

function show(path: string) {
  saveAuth({
    token: "jwt",
    user: {
      id: 1,
      firstName: "Ann",
      lastName: "Admin",
      email: "admin@example.com",
      role: "ADMIN",
      status: "ACTIVE",
    },
  });

  const client = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <AuthProvider>
          <App />
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

beforeEach(() => {
  localStorage.clear();
  vi.resetAllMocks();
  vi.mocked(auditApi.fetchAuditLogs).mockResolvedValue({
    content: [audit],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
  });
  vi.mocked(auditApi.fetchAuditLog).mockResolvedValue(audit);
});

it("lists and filters safe audit records", async () => {
  show("/admin/audit-logs");

  expect(await screen.findByText("ADMIN_USER_DISABLE")).toBeInTheDocument();
  expect(screen.getByText("request-42")).toBeInTheDocument();

  fireEvent.change(screen.getByLabelText("Audit result"), {
    target: { value: "SUCCESS" },
  });

  await waitFor(() =>
    expect(auditApi.fetchAuditLogs).toHaveBeenLastCalledWith(
      expect.objectContaining({ result: "SUCCESS" }),
    ),
  );
});

it("shows audit details without sensitive payloads", async () => {
  show("/admin/audit-logs/4");

  expect(
    await screen.findByRole("heading", { name: "ADMIN_USER_DISABLE" }),
  ).toBeInTheDocument();
  expect(screen.getByText("request-42")).toBeInTheDocument();
  expect(screen.queryByText(/password|token/i)).not.toBeInTheDocument();
});

it("renders the empty audit state", async () => {
  vi.mocked(auditApi.fetchAuditLogs).mockResolvedValueOnce({
    content: [],
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });

  show("/admin/audit-logs");

  expect(
    await screen.findByText("No audit records match these filters."),
  ).toBeInTheDocument();
});