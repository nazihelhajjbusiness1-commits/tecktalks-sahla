import type { AuthSession, Credentials, User, UserRole } from '@/types'
import { currentUser } from './mockData'
import { brand } from '@/config/brand'
import { USE_MOCKS, mockDelay, request, setToken } from './api'

const SESSION_KEY = 'sahla.user'

/** Backend Role enum -> frontend role code. */
const roleMap: Record<string, UserRole> = {
  ADMIN: 'admin',
  MANAGER: 'manager',
  RECEIVING_EMPLOYEE: 'receiving',
  INSPECTOR: 'inspector',
  ACCOUNTANT: 'accountant',
  WAREHOUSE_EMPLOYEE: 'warehouse',
}

/** Shape of the backend UserResponse returned by /api/auth/me. */
interface BackendUser {
  id: number
  firstname: string
  lastname: string
  username: string
  email: string
  phoneNumber?: string
  role: string
}

/** Backend wraps auth payloads in ApiResponse<T> { success, message, data }. */
interface ApiEnvelope<T> {
  data: T
}

function mapUser(b: BackendUser): User {
  return {
    id: String(b.id),
    name: `${b.firstname} ${b.lastname}`.trim() || b.username,
    email: b.email,
    role: roleMap[b.role] ?? 'warehouse',
    cooperative: brand.name,
  }
}

/**
 * Authentication. Every screen reads the user through `useAuth`, so wiring
 * the real Spring Boot flow lives entirely here.
 *
 * Real flow: POST /auth/login returns a JWT at `data.token`; we store it,
 * then GET /auth/me (Bearer) to resolve the full user profile + role.
 * Mock flow: any non-empty credentials succeed except the reserved
 * "wrong@demo" identifier, which exercises the invalid-credentials state.
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

    const loginRes = await request<ApiEnvelope<{ token: string }>>(
      '/auth/login',
      {
        method: 'POST',
        json: { email: credentials.identifier, password: credentials.password },
      },
    )
    const token = loginRes.data.token
    setToken(token)

    const meRes = await request<ApiEnvelope<BackendUser>>('/auth/me')
    const user = mapUser(meRes.data)
    localStorage.setItem(SESSION_KEY, JSON.stringify(user))
    return { user, token }
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
