import axios from "axios";
import { clearAuth, loadAuth } from "../features/auth/authStorage";
import { runtimeConfig } from "../config";

export const AUTH_UNAUTHORIZED_EVENT = "markethub:unauthorized";

const client = axios.create({
  baseURL: runtimeConfig.apiBaseUrl,
  headers: {
    "Content-Type": "application/json",
  },
});

client.interceptors.request.use((config) => {
  const token = loadAuth()?.token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
      window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT));
    }
    return Promise.reject(error);
  },
);

export default client;