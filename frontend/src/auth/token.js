// Token kept in localStorage — a documented shortcut (see README), not an
// httpOnly cookie. Fine for a single-instance demo, not for production.
const TOKEN_KEY = 'hardwarehub_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}
