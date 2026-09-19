import axios from "axios";

export interface NormalizedApiError {
  status?: number;
  message: string;
  retryAfterSeconds?: number;
}

export function normalizeApiError(
  error: unknown,
  fallback = "Something went wrong",
): NormalizedApiError {
  if (axios.isAxiosError(error)) {
    const retryHeader = Number(error.response?.headers?.["retry-after"]);
    return {
      status: error.response?.status,
      message:
        error.response?.status === 429
          ? (error.response?.data?.message ?? "Too many requests. Please try again later.")
          : (error.response?.data?.message ?? fallback),
      retryAfterSeconds: Number.isFinite(retryHeader) ? retryHeader : undefined,
    };
  }
  return {
    message: error instanceof Error ? error.message : fallback,
  };
}

export function apiErrorMessage(error: unknown, fallback: string) {
  return normalizeApiError(error, fallback).message;
}