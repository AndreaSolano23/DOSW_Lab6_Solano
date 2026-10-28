import { API_URL, authFetch, handleResponse } from './http'

const JSON_HEADERS = { 'Content-Type': 'application/json' }

// Login y registro son públicos: usan fetch normal. Así un 401 por
// contraseña incorrecta no se confunde con una sesión expirada.
export async function register({ name, email, password }) {
  return handleResponse(
    await fetch(`${API_URL}/auth/register`, {
      method: 'POST',
      headers: JSON_HEADERS,
      body: JSON.stringify({ name, email, password }),
    }),
  )
}

export async function login({ email, password }) {
  return handleResponse(
    await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: JSON_HEADERS,
      body: JSON.stringify({ email, password }),
    }),
  )
}

// Perfil del usuario autenticado (RF-SEG-04). Requiere token.
export async function getMe() {
  return handleResponse(await authFetch(`${API_URL}/users/me`))
}
