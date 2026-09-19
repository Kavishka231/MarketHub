import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: Number(__ENV.VUS || 5),
  duration: __ENV.DURATION || "30s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<750"],
  },
};

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";

export default function () {
  const page = http.get(`${baseUrl}/api/products?page=0&size=20&sort=newest`);
  check(page, { "catalog returns 200": (response) => response.status === 200 });

  const search = http.get(`${baseUrl}/api/products?search=phone&page=0&size=20`);
  check(search, { "search returns 200": (response) => response.status === 200 });

  if (__ENV.PRODUCT_ID) {
    const detail = http.get(`${baseUrl}/api/products/${__ENV.PRODUCT_ID}`);
    check(detail, { "product detail returns 200": (response) => response.status === 200 });
  }
  sleep(1);
}