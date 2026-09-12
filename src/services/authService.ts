import type { AuthSession, Credentials, User } from '@/types'
import { currentUser } from './mockData'
import { USE_MOCKS, mockDelay, request, setToken } from './api'

const SESSION_KEY = 'mawsim.user'

/**
 * Development / mock authentication.
 * Any non-empty credentials succeed EXCEPT the reserved "wrong@demo"
 * identifier, which lets us exercise the invalid-credentials state.
 * Replace `USE_MOCKS` branch with the real `request` call to wire up
 * Spring Boot without touching the login screen.
 */
export const authService = {
  async login(credentials: Credentials): Promise<AuthSession> {
    if (USE_MOCKS) {
      if (
        !credentials.identifier.trim() ||
        !credentials.password.trim() ||
        credentials.identifier.trim().toLowerCase() === 'wrong@demo'
      ) {
        await mockDelay(null, 600)
        throw new Error('Invalid email or password. Please try again.')
      }
      const session: AuthSession = {
        user: currentUser,
        token: 'mock-token-dev',
      }
      await mockDelay(session, 700)
      setToken(session.token)
      localStorage.setItem(SESSION_KEY, JSON.stringify(session.user))
      return session
    }

    const session = await request<AuthSession>('/auth/login', {
      method: 'POST',
      json: credentials,
    })
    setToken(session.token)
    localStorage.setItem(SESSION_KEY, JSON.stringify(session.user))
    return session
  },

  logout(): void {
    setToken(null)
    localStorage.removeItem(SESSION_KEY)
  },

  /** Restore the persisted user on app load (mock or real token flow). */
  getStoredUser(): User | null {
    const raw = localStorage.getItem(SESSION_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw) as User
    } catch {
      return null
    }
  },
}
