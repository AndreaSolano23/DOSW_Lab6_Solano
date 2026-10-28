// El token vive SOLO en memoria (variable del módulo). No se guarda en
// localStorage, sessionStorage ni cookies: al recargar la página se pierde.
let accessToken = null
let unauthorizedHandler = null

export function setAccessToken(token) {
  accessToken = token
}

export function getAccessToken() {
  return accessToken
}

// AuthContext registra aquí qué hacer cuando el backend responde 401.
export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

export function notifyUnauthorized() {
  if (unauthorizedHandler) {
    unauthorizedHandler()
  }
}
