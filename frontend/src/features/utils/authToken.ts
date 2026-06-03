const TOKEN_KEY = "auth_token";
const JWT_PATTERN = /^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/;

export function saveToken(token: unknown) {
  if (typeof token !== "string" || !JWT_PATTERN.test(token)) {
    return;
  }

  localStorage.setItem(TOKEN_KEY, token);
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

