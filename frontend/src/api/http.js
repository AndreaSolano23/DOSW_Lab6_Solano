import { getAccessToken, notifyUnauthorized } from './session'

export const API_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1'

// Convierte la respuesta en datos, o lanza un Error con un mensaje legible.
export async function handleResponse(response) {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`
    try {
      const body = await response.json()
      if (body.errors) {
        // Errores de validación del backend (400): { campo: mensaje }
        message = Object.values(body.errors).join('. ')
      } else if (body.message) {
        message = body.message
      }
    } catch {
      // La respuesta no traía JSON; se deja el mensaje por defecto.
    }
    const error = new Error(message)
    error.status = response.status
    throw error
  }
  if (response.status === 204) {
    return null
  }
  return response.json()
}

// fetch para recursos protegidos: agrega "Authorization: Bearer <token>"
// y, si el backend responde 401, avisa para limpiar la sesión.
export async function authFetch(url, options = {}) {
  const token = getAccessToken()
  const headers = { ...options.headers }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const response = await fetch(url, { ...options, headers })
  if (response.status === 401) {
    notifyUnauthorized()
  }
  return response
}
