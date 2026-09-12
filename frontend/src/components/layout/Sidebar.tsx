import { NavLink } from 'react-router-dom'
import { PanelLeftClose, PanelLeftOpen } from 'lucide-react'
import { cn } from '@/utils/cn'
import { navItems } from '@/config/nav'
import { Logo } from './Logo'

interface SidebarProps {
  /** Desktop icon-only rail. */
  collapsed: boolean
  /** Toggle the desktop rail (hidden on mobile drawer). */
  onToggleCollapse?: () => void
  /** Called when a link is chosen — used to close the mobile drawer. */
  onNavigate?: () => void
  /** Hide the collapse toggle (mobile drawer). */
  hideCollapseToggle?: boolean
}

/**
 * Primary navigation panel. Deep-olive surface so it reads as the app's
 * anchor without flooding the workspace with green. Used both as the
 * persistent desktop sidebar and inside the mobile drawer.
 */
export function Sidebar({
  collapsed,
  onToggleCollapse,
  onNavigate,
  hideCollapseToggle = false,
}: SidebarProps) {
  return (
    <div className="flex h-full flex-col bg-primary text-on-primary">
      <div
        className={cn(
          'flex h-16 items-center border-b border-on-primary/10',
          collapsed ? 'justify-center px-2' : 'px-4',
        )}
      >
        <Logo inverted markOnly={collapsed} />
      </div>

      <nav
        className="flex-1 space-y-1 overflow-y-auto scrollbar-thin px-3 py-4"
        aria-label="Primary"
      >
        {navItems.map(({ label, to, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onNavigate}
            title={collapsed ? label : undefined}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors',
                'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-on-primary',
                collapsed && 'justify-center',
                isActive
                  ? 'bg-on-primary/15 text-on-primary'
                  : 'text-on-primary/75 hover:bg-on-primary/10 hover:text-on-primary',
              )
            }
          >
            <Icon className="h-5 w-5 shrink-0" aria-hidden="true" />
            {!collapsed && <span className="truncate">{label}</span>}
          </NavLink>
        ))}
      </nav>

      {!hideCollapseToggle && (
        <div className="border-t border-on-primary/10 p-3">
          <button
            type="button"
            onClick={onToggleCollapse}
            className={cn(
              'flex w-full items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium text-on-primary/75 transition-colors hover:bg-on-primary/10 hover:text-on-primary cursor-pointer',
              collapsed && 'justify-center',
            )}
            aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? (
              <PanelLeftOpen className="h-5 w-5 shrink-0" aria-hidden="true" />
            ) : (
              <>
                <PanelLeftClose className="h-5 w-5 shrink-0" aria-hidden="true" />
                <span>Collapse</span>
              </>
            )}
          </button>
        </div>
      )}
    </div>
  )
}
