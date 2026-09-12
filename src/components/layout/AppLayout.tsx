import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { X } from 'lucide-react'
import { cn } from '@/utils/cn'
import { pageTitleForPath } from '@/config/nav'
import { Sidebar } from './Sidebar'
import { Header } from './Header'

/**
 * Authenticated application shell.
 *  - lg+: persistent sidebar (collapsible to an icon rail)
 *  - < lg: sidebar hidden; opened as an overlay drawer from the header
 * The main region scrolls independently so the header/sidebar stay put.
 */
export function AppLayout() {
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  // Close the mobile drawer whenever the route changes (syncing UI with the
  // router — including programmatic navigation like logout).
  useEffect(() => {
    // oxlint-disable-next-line react/set-state-in-effect
    setMobileOpen(false)
  }, [location.pathname])

  const title = pageTitleForPath(location.pathname)

  return (
    <div className="flex h-dvh overflow-hidden bg-background">
      {/* Desktop sidebar */}
      <aside
        className={cn(
          'hidden shrink-0 transition-[width] duration-200 lg:block',
          collapsed ? 'w-[76px]' : 'w-64',
        )}
      >
        <Sidebar
          collapsed={collapsed}
          onToggleCollapse={() => setCollapsed((v) => !v)}
        />
      </aside>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="absolute inset-0 bg-foreground/40 motion-safe:animate-[fadeIn_150ms_ease-out]"
            onClick={() => setMobileOpen(false)}
            aria-hidden="true"
          />
          <div className="absolute inset-y-0 start-0 w-72 max-w-[80%] shadow-raised motion-safe:animate-[slideInStart_200ms_ease-out]">
            <button
              type="button"
              onClick={() => setMobileOpen(false)}
              aria-label="Close navigation menu"
              className="absolute end-3 top-4 z-10 flex h-9 w-9 items-center justify-center rounded-md text-on-primary/80 hover:bg-on-primary/10 cursor-pointer"
            >
              <X className="h-5 w-5" aria-hidden="true" />
            </button>
            <Sidebar
              collapsed={false}
              hideCollapseToggle
              onNavigate={() => setMobileOpen(false)}
            />
          </div>
        </div>
      )}

      {/* Main column */}
      <div className="flex min-w-0 flex-1 flex-col">
        <Header title={title} onOpenMobileNav={() => setMobileOpen(true)} />
        <main className="flex-1 overflow-y-auto scrollbar-thin">
          <div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
