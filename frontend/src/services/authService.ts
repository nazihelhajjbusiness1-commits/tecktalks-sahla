import type { AuthSession, Credentials, User, UserRole } from '@/types'
import { currentUser } from './mockData'
import { USE_MOCKS, mockDelay, request, setToken } from './api'

const SESSION_KEY = 'sahla.user'

/** Backend Role enum (com.farmmanagement.backend.auth.Role) -> frontend UserRole. */
const BACKEND_ROLE_TO_USER_ROLE: Record<string, UserRole> = {
  ADMIN: 'admin',
  MANAGER: 'manager',
  RECEIVING_EMPLOYEE: 'receiving',
  ACCOUNTANT: 'accountant',
  WAREHOUSE_EMPLOYEE: 'warehouse',
  INSPECTOR: 'inspector',
}

interface BackendUser {
  id: number
  firstname: string
  lastname: string
  email: string
  role: string
}

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

    // Backend expects { email, password } (LoginRequest.java) and wraps the
    // response in ApiResponse<AuthResponse> - { success, message, data: { token } }.
    // AuthResponse carries no user info, so a follow-up /auth/me call fills it in.
    const loginResponse = await request<{ data: { token: string } }>('/auth/login', {
      method: 'POST',
      json: { email: credentials.identifier, password: credentials.password },
    })
    const token = loginResponse.data.token
    setToken(token)

    const meResponse = await request<{ data: BackendUser }>('/auth/me')
    const backendUser = meResponse.data
    const user: User = {
      id: String(backendUser.id),
      name: `${backendUser.firstname} ${backendUser.lastname}`,
      email: backendUser.email,
      role: BACKEND_ROLE_TO_USER_ROLE[backendUser.role] ?? 'warehouse',
      // Backend has no concept of "cooperative" yet (not on the User entity
      // or any DTO) - left blank rather than inventing a value.
      cooperative: '',
    }

    const session: AuthSession = { user, token }
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
