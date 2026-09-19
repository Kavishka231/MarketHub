import { render, screen } from "@testing-library/react";
import { ErrorBoundary } from "./ErrorBoundary";

function BrokenComponent(): never {
  throw new Error("Sensitive implementation detail");
}

describe("ErrorBoundary", () => {
  it("shows a safe recovery screen without exposing exception details", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined);
    render(
      <ErrorBoundary>
        <BrokenComponent />
      </ErrorBoundary>,
    );

    expect(screen.getByRole("alert")).toHaveTextContent("Something went wrong");
    expect(screen.getByRole("button", { name: "Reload MarketHub" })).toBeEnabled();
    expect(screen.queryByText("Sensitive implementation detail")).not.toBeInTheDocument();
  });
});