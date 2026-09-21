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
} from 'lucide-react'

export interface NavItem {
  label: string
  to: string
  icon: LucideIcon
}

export const navItems: NavItem[] = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
  { label: 'Farmers', to: '/farmers', icon: Users },
  { label: 'Deliveries', to: '/deliveries', icon: Truck },
  { label: 'Inventory', to: '/inventory', icon: Boxes },
  { label: 'Payments', to: '/payments', icon: Wallet },
  { label: 'Reports', to: '/reports', icon: FileBarChart },
  { label: 'Settings', to: '/settings', icon: Settings },
]

/** Human-readable page title for a given pathname (used in the header). */
export function pageTitleForPath(pathname: string): string {
  if (pathname.startsWith('/farmers/')) return 'Farmer Details'
  if (pathname.startsWith('/settings/products/')) return 'Product Settings'
  if (pathname.startsWith('/settings/products')) return 'Products'
  const match = navItems.find((item) => pathname.startsWith(item.to))
  return match?.label ?? 'Sahla'
}
