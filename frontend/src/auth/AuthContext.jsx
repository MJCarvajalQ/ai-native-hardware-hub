import { createContext, useContext, useState, useCallback } from 'react'
import { api } from '../api/client'
import { getToken, setToken, clearToken, getUser, setUser, clearUser } from './token'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getToken())
  const [user, setUserState] = useState(() => getUser())

  const login = useCallback(async (email, password) => {
    const data = await api.post('/auth/login', { email, password }, { auth: false })
    setToken(data.token)
    setUser({ email: data.email, role: data.role })
    setUserState({ email: data.email, role: data.role })
    setIsAuthenticated(true)
  }, [])

  const logout = useCallback(() => {
    clearToken()
    clearUser()
    setUserState(null)
    setIsAuthenticated(false)
  }, [])

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
