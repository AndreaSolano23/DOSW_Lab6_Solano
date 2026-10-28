import { useCallback, useEffect, useMemo, useState } from 'react'
import * as authApi from '../../../api/authApi'
import { setAccessToken, setUnauthorizedHandler } from '../../../api/session'

import { AuthContext } from './authContextValue'

const EXPIRED_MESSAGE = 'Your session has expired. Please log in again.'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [expiresAt, setExpiresAt] = useState(null)
  const [sessionNotice, setSessionNotice] = useState('')

  // Borra todo rastro de la sesión: token, usuario y estado de la UI.
  const clearSession = useCallback((notice = '') => {
    setAccessToken(null)
    setUser(null)
    setExpiresAt(null)
    setSessionNotice(notice)
  }, [])

  // Si cualquier petición protegida recibe 401, se cierra la sesión.
  useEffect(() => {
    setUnauthorizedHandler(() => clearSession(EXPIRED_MESSAGE))
    return () => setUnauthorizedHandler(null)
  }, [clearSession])

  // Cierra la sesión sola cuando vence el token (expiresIn viene del login).
  useEffect(() => {
    if (!expiresAt) {
      return undefined
    }
    const timer = setTimeout(
      () => clearSession(EXPIRED_MESSAGE),
      Math.max(expiresAt - Date.now(), 0),
    )
    return () => clearTimeout(timer)
  }, [expiresAt, clearSession])

  const login = useCallback(async (email, password) => {
    const result = await authApi.login({ email, password })
    // El token queda en memoria; getMe ya lo envía en el header.
    setAccessToken(result.accessToken)
    try {
      const profile = await authApi.getMe()
      setUser(profile)
      setExpiresAt(Date.now() + result.expiresIn * 1000)
      setSessionNotice('')
    } catch (error) {
      setAccessToken(null)
      throw error
    }
  }, [])

  const register = useCallback(
    (name, email, password) => authApi.register({ name, email, password }),
    [],
  )

  const logout = useCallback(() => clearSession(''), [clearSession])

  const value = useMemo(
    () => ({ user, isAuthenticated: user !== null, sessionNotice, login, register, logout }),
    [user, sessionNotice, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
