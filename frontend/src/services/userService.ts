import type {
  CreateStaffUserRequest,
  StaffUser,
  UpdateStaffUserRequest,
} from '@/types'
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

  /** Change a user's role and/or active status. */
  async update(id: number, body: UpdateStaffUserRequest): Promise<StaffUser> {
    const res = await request<ApiEnvelope<StaffUser>>(`/auth/users/${id}`, {
      method: 'PATCH',
      json: body,
    })
    return res.data
  },

  /** Set a new password for a user account. */
  async resetPassword(id: number, newPassword: string): Promise<void> {
    await request(`/auth/users/${id}/reset-password`, {
      method: 'POST',
      json: { newPassword },
    })
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
