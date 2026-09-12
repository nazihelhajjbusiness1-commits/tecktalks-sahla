import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type { Credentials, User } from '@/types'
import { authService } from '@/services/authService'

/**
 * Authentication context.
 * Backed by the mock `authService` for Week 1. Because every screen reads
 * the user through this hook (never the service directly), swapping in the
 * real Spring Boot flow is a change confined to `authService`.
 */
interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  login: (credentials: Credentials) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() =>
    authService.getStoredUser(),
  )

  const login = useCallback(async (credentials: Credentials) => {
    const session = await authService.login(credentials)
    setUser(session.user)
  }, [])

  const logout = useCallback(() => {
    authService.logout()
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ user, isAuthenticated: user !== null, login, logout }),
    [user, login, logout],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
