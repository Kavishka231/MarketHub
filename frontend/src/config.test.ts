import { resolveApiBaseUrl } from "./config";

describe("runtime configuration", () => {
  it("normalizes a configured API URL", () => {
    expect(resolveApiBaseUrl("https://api.example.com/", false)).toBe("https://api.example.com");
  });

  it("uses localhost only during development", () => {
    expect(resolveApiBaseUrl(undefined, true)).toBe("http://localhost:8080");
    expect(() => resolveApiBaseUrl(undefined, false)).toThrow("VITE_API_BASE_URL is required");
  });

  it("rejects unsafe and relative URLs", () => {
    expect(() => resolveApiBaseUrl("javascript:alert(1)", false)).toThrow("HTTP or HTTPS");
    expect(() => resolveApiBaseUrl("/api", false)).toThrow("absolute");
  });
});