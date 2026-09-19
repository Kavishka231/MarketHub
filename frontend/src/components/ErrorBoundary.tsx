import { Component, type ErrorInfo, type ReactNode } from "react";

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false };

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(_error: Error, _info: ErrorInfo) {
    // Production telemetry can be connected here without exposing details to users.
  }

  render() {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <main className="mx-auto flex min-h-screen max-w-xl items-center px-6 text-center">
        <section role="alert" className="w-full rounded-2xl border bg-white p-8 shadow-sm">
          <h1 className="text-2xl font-semibold text-slate-900">Something went wrong</h1>
          <p className="mt-3 text-slate-600">
            MarketHub could not display this page. No private error details are shown.
          </p>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="mt-6 rounded-lg bg-slate-900 px-4 py-2 font-medium text-white"
          >
            Reload MarketHub
          </button>
        </section>
      </main>
    );
  }
}