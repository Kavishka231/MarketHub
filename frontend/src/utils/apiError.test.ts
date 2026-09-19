import { AxiosError, AxiosHeaders } from "axios";
import { normalizeApiError } from "./apiError";

describe("normalizeApiError", () => {
  it("normalizes rate limits and retry metadata", () => {
    const error = new AxiosError("limited", "429", undefined, undefined, {
      data: { message: "Please slow down" },
      status: 429,
      statusText: "Too Many Requests",
      headers: new AxiosHeaders({ "retry-after": "60" }),
      config: { headers: new AxiosHeaders() },
    });

    expect(normalizeApiError(error)).toEqual({
      status: 429,
      message: "Please slow down",
      retryAfterSeconds: 60,
    });
  });
});