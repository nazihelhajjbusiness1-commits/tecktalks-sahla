import type { CreateStaffUserRequest, StaffUser } from '@/types'
import { request } from './api'

/** Backend wraps these payloads in ApiResponse<T> { success, message, data }. */
interface ApiEnvelope<T> {
  data: T
}

/**
 * Admin user management (Create Account). Both endpoints are ADMIN-gated on
 * the backend; the UI only exposes them to admins. New accounts then log in
 * through the normal login screen.
 */
export const userService = {
  async list(): Promise<StaffUser[]> {
    const res = await request<ApiEnvelope<StaffUser[]>>('/auth/users')
    return res.data
  },

  async create(body: CreateStaffUserRequest): Promise<StaffUser> {
    const res = await request<ApiEnvelope<StaffUser>>('/auth/register', {
      method: 'POST',
      json: body,
    })
    return res.data
  },
}

/** Human-readable labels for the backend roles. */
export const roleLabels: Record<CreateStaffUserRequest['role'], string> = {
  ADMIN: 'Administrator',
  MANAGER: 'Manager',
  RECEIVING_EMPLOYEE: 'Receiving Employee',
  ACCOUNTANT: 'Accountant',
  WAREHOUSE_EMPLOYEE: 'Warehouse Employee',
  INSPECTOR: 'Inspector',
}
