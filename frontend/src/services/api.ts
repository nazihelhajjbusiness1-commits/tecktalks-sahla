/**
 * Thin HTTP client + mock toggle.
 *
 * Services centralize the base URL, auth header, and a small `request`
 * helper here. Sprint 2 talks to the real Spring Boot REST API by default;
 * set `VITE_USE_MOCKS=true` to fall back to local mock data for offline
 * frontend work — no component changes required either way.
 *
 * Configure the backend base URL with `VITE_API_URL`
 * (e.g. http://localhost:8080/api). Defaults to `/api` for same-origin / proxy.
 */

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api'

/** Global switch. When true, services resolve local mock data. */
export const USE_MOCKS =
  (import.meta.env.VITE_USE_MOCKS ?? 'false') === 'true'

const TOKEN_KEY = 'sahla.token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null): void {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export interface RequestOptions extends RequestInit {
  /** Parsed as JSON and sent as the request body. */
  json?: unknown
}

/**
 * Fetch wrapper that attaches the auth token and parses JSON.
 * Used by services once the real backend is connected.
 */
export async function request<T>(
  path: string,
  { json, headers, ...init }: RequestOptions = {},
): Promise<T> {
  const token = getToken()
  const res = await fetch(`${BASE_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: json !== undefined ? JSON.stringify(json) : init.body,
  })

  if (!res.ok) {
    // Backend errors are JSON: ErrorResponse { message } or ApiResponse { message }.
    // Fall back to raw text / status when the body isn't JSON.
    const raw = await res.text().catch(() => '')
    let message = raw || res.statusText
    if (raw) {
      try {
        const body = JSON.parse(raw) as { message?: string }
        if (body?.message) message = body.message
      } catch {
        /* not JSON — keep raw text */
      }
    }
    throw new ApiError(res.status, message || 'Request failed')
  }

  // 204 No Content
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}

export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/** Simulate network latency for mock services so loading states are real. */
export function mockDelay<T>(data: T, ms = 450): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(data), ms))
}

/**
 * Build a `?key=value` query string from a params object, skipping
 * undefined/null/empty values. Returns '' when there is nothing to add.
 */
export function buildQuery(
  params: Record<string, string | number | boolean | undefined | null>,
): string {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue
    search.set(key, String(value))
  }
  const qs = search.toString()
  return qs ? `?${qs}` : ''
}
