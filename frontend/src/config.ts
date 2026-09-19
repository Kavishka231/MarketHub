const DEFAULT_DEVELOPMENT_API_URL = "http://localhost:8080";

export function resolveApiBaseUrl(value: string | undefined, development: boolean): string {
  const candidate = value?.trim();

  if (!candidate) {
    if (development) {
      return DEFAULT_DEVELOPMENT_API_URL;
    }
    throw new Error("VITE_API_BASE_URL is required for a production build");
  }

  let url: URL;
  try {
    url = new URL(candidate);
  } catch {
    throw new Error("VITE_API_BASE_URL must be an absolute HTTP(S) URL");
  }

  if (!['http:', 'https:'].includes(url.protocol)) {
    throw new Error("VITE_API_BASE_URL must use HTTP or HTTPS");
  }

  return candidate.replace(/\/$/, "");
}

export const runtimeConfig = {
  apiBaseUrl: resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL, import.meta.env.DEV),
};