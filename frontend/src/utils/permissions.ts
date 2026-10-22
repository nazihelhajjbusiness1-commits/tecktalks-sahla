/**
 * DT-93: frontend view of the backend role matrix. The backend is always the
 * real authority (every endpoint is role-guarded); this helper lets the UI
 * hide or disable actions a user cannot perform so they never hit a 403.
 */
import type { UserRole } from '@/types'

export type Action =
  | 'farmer.create'
  | 'farmer.edit'
  | 'delivery.create'
  | 'delivery.weigh'
  | 'delivery.grade'
  | 'settlement.confirm'
  | 'payment.record'
  | 'pricing.manage'
  | 'reports.view'
  | 'dashboard.view'
  | 'users.manage'

const matrix: Record<Action, UserRole[]> = {
  'farmer.create': ['admin', 'manager', 'receiving'],
  'farmer.edit': ['admin', 'manager'],
  'delivery.create': ['admin', 'manager', 'receiving'],
  'delivery.weigh': ['admin', 'manager', 'receiving'],
  'delivery.grade': ['admin', 'manager', 'inspector'],
  'settlement.confirm': ['admin', 'manager', 'accountant'],
  'payment.record': ['admin', 'manager', 'accountant'],
  'pricing.manage': ['admin', 'manager'],
  'reports.view': ['admin', 'manager', 'accountant', 'warehouse'],
  'dashboard.view': ['admin', 'manager', 'accountant'],
  'users.manage': ['admin'],
}

/** Whether a role may perform an action. Unknown/absent role => false. */
export function can(role: UserRole | undefined | null, action: Action): boolean {
  if (!role) return false
  return matrix[action].includes(role)
}
