/**
 * Thin HTTP client + mock toggle.
 *
 * Week 1 runs entirely on mock data (see mockData.ts and the *Service
 * modules). This module centralizes the base URL, auth header, and a
 * small `request` helper so switching to the real Spring Boot REST API
 * later means flipping `USE_MOCKS` to false and pointing VITE_API_URL at
 * the backend — no component changes required.
 */

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api'

/** Global switch. When true, services resolve local mock data. */
export const USE_MOCKS =
  (import.meta.env.VITE_USE_MOCKS ?? 'true') !== 'false'

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
    const message = await res.text().catch(() => res.statusText)
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
