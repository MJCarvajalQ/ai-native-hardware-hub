// Token and user info kept in localStorage — a documented shortcut (see
// README), not an httpOnly cookie. Fine for a single-instance demo, not for
// production.
const TOKEN_KEY = 'hardwarehub_token'
const USER_KEY = 'hardwarehub_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearUser() {
  localStorage.removeItem(USER_KEY)
}
