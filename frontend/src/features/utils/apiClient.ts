import axios from "axios";
import { CONFIG } from "../../config/config";
import { clearToken, getToken } from "./authToken";

axios.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearToken();
      localStorage.removeItem("auth_user");
    }
    return Promise.reject(error);
  }
);

export async function authFetch(path: string, options: RequestInit = {}) {
  const token = getToken();
  const headers = new Headers(options.headers);

  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const res = await fetch(`${CONFIG.API_BASE}${path}`, {
    ...options,
    headers,
  });
  
//   console.log("Authorization header:", headers.get("Authorization"));
  if (res.status === 401) {
    clearToken();
    localStorage.removeItem("auth_user");
  }

  return res;
}

export function buildAuthUrl(path: string) {
  const token = getToken();
  const url = new URL(`${CONFIG.API_BASE}${path}`);

  if (token) {
    url.searchParams.set("token", token);
  }

  return url.toString();
}
