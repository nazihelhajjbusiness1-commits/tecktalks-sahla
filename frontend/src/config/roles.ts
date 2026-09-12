/** Friendly labels for the internal role codes. */
import type { UserRole } from '@/types'

export const roleLabels: Record<UserRole, string> = {
  admin: 'Administrator',
  manager: 'Cooperative Manager',
  receiving: 'Receiving Employee',
  inspector: 'Quality Inspector',
  accountant: 'Accountant',
  warehouse: 'Warehouse Employee',
}
