/**
 * Sidebar navigation model. Single source of truth for the app's primary
 * routes so the sidebar, mobile drawer, and page-title lookup stay in sync.
 * Icons are Lucide component names resolved in the Sidebar.
 */
import type { LucideIcon } from 'lucide-react'
import {
  LayoutDashboard,
  Users,
  Truck,
  Boxes,
  Wallet,
  FileBarChart,
  Settings,
  ClipboardCheck,
  Gavel,
  UserCog,
} from 'lucide-react'
import type { UserRole } from '@/types'

export interface NavItem {
  label: string
  to: string
  icon: LucideIcon
  /**
   * Roles allowed to see/use this section. Mirrors the backend
   * authorization rules in SecurityConfig so the sidebar never shows a
   * destination the API would reject with 403.
   */
  roles: UserRole[]
}

export const navItems: NavItem[] = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard, roles: ['admin', 'manager', 'accountant'] },
  { label: 'Users', to: '/users', icon: UserCog, roles: ['admin'] },
  { label: 'Quality', to: '/quality', icon: ClipboardCheck, roles: ['admin', 'manager', 'inspector'] },
  { label: 'Grade Reviews', to: '/grade-reviews', icon: Gavel, roles: ['admin', 'manager'] },
  { label: 'Farmers', to: '/farmers', icon: Users, roles: ['admin', 'manager', 'receiving', 'accountant'] },
  { label: 'Deliveries', to: '/deliveries', icon: Truck, roles: ['admin', 'manager', 'receiving', 'accountant', 'warehouse', 'inspector'] },
  { label: 'Inventory', to: '/inventory', icon: Boxes, roles: ['admin', 'manager', 'warehouse', 'accountant'] },
  { label: 'Payments', to: '/payments', icon: Wallet, roles: ['admin', 'manager', 'accountant'] },
  { label: 'Reports', to: '/reports', icon: FileBarChart, roles: ['admin', 'manager', 'accountant', 'warehouse'] },
  { label: 'Settings', to: '/settings', icon: Settings, roles: ['admin', 'manager'] },
]

/** Nav sections the given role is allowed to see, in sidebar order. */
export function navItemsForRole(role: UserRole): NavItem[] {
  return navItems.filter((item) => item.roles.includes(role))
}

/**
 * Preferred landing route per role — the screen that *is* their job — when it
 * differs from the first sidebar item. Receiving staff work in Deliveries;
 * warehouse staff in Inventory. Others fall back to their first nav section.
 */
const roleHomeOverrides: Partial<Record<UserRole, string>> = {
  receiving: '/deliveries',
  warehouse: '/inventory',
}

/**
 * Landing route for a role after login. Uses the per-role override when set
 * (and allowed), otherwise the first nav section they can use. Falls back to
 * the always-available profile page if a role has no sections.
 */
export function homePathForRole(role: UserRole): string {
  const override = roleHomeOverrides[role]
  const allowed = navItemsForRole(role)
  if (override && allowed.some((item) => item.to === override)) {
    return override
  }
  return allowed[0]?.to ?? '/profile'
}

/**
 * Whether a role may open a given pathname. /profile is always allowed;
 * every other path is matched to its nav section and checked against that
 * section's roles. Unknown paths (e.g. 404s) are permitted so the
 * NotFound page can render.
 */
export function isPathAllowedForRole(pathname: string, role: UserRole): boolean {
  if (pathname === '/profile' || pathname.startsWith('/profile/')) return true
  const section = navItems.find(
    (item) => pathname === item.to || pathname.startsWith(item.to + '/'),
  )
  if (!section) return true
  return section.roles.includes(role)
}

/** Human-readable page title for a given pathname (used in the header). */
export function pageTitleForPath(pathname: string): string {
  if (pathname.startsWith('/farmers/')) return 'Farmer Details'
  if (pathname.startsWith('/deliveries/new')) return 'New Delivery'
  if (/^\/deliveries\/[^/]+\/settlement/.test(pathname)) return 'Settlement'
  if (pathname.startsWith('/deliveries/')) return 'Delivery Details'
  if (pathname.startsWith('/settings/products/')) return 'Product Settings'
  if (pathname.startsWith('/settings/products')) return 'Products'
  const match = navItems.find((item) => pathname.startsWith(item.to))
  return match?.label ?? 'Sahla'
}
