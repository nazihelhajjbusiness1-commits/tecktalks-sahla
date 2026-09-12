import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Menu, Bell, ChevronDown, LogOut, UserRound, Search } from 'lucide-react'
import { cn } from '@/utils/cn'
import { useAuth } from '@/hooks/useAuth'
import { roleLabels } from '@/config/roles'
import { initials } from '@/utils/format'

interface HeaderProps {
  title: string
  /** Opens the mobile navigation drawer. */
  onOpenMobileNav: () => void
}

export function Header({ title, onOpenMobileNav }: HeaderProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!menuOpen) return
    const onClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false)
      }
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMenuOpen(false)
    }
    document.addEventListener('mousedown', onClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [menuOpen])

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center gap-3 border-b border-border bg-card/95 px-4 backdrop-blur sm:px-6">
      <button
        type="button"
        onClick={onOpenMobileNav}
        className="flex h-10 w-10 items-center justify-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground lg:hidden cursor-pointer"
        aria-label="Open navigation menu"
      >
        <Menu className="h-5 w-5" aria-hidden="true" />
      </button>

      <h1 className="font-heading text-lg font-semibold text-foreground truncate">
        {title}
      </h1>

      {/* Optional global search — hidden on small screens to save room. */}
      <div className="relative ms-auto hidden md:block">
        <label htmlFor="global-search" className="sr-only">
          Search
        </label>
        <Search
          className="pointer-events-none absolute inset-y-0 start-3 my-auto h-4.5 w-4.5 text-muted-foreground"
          aria-hidden="true"
        />
        <input
          id="global-search"
          type="search"
          placeholder="Search farmers, deliveries…"
          className="h-10 w-56 rounded-md border border-input bg-background ps-10 pe-3 text-sm text-foreground placeholder:text-muted-foreground/70 transition-[width] focus-visible:w-72 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring lg:w-64 [&::-webkit-search-cancel-button]:hidden"
        />
      </div>

      <div className="flex items-center gap-1 ms-auto md:ms-3">
        <button
          type="button"
          className="relative flex h-10 w-10 items-center justify-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer"
          aria-label="Notifications (3 unread)"
        >
          <Bell className="h-5 w-5" aria-hidden="true" />
          <span
            className="absolute end-2 top-2 h-2 w-2 rounded-full bg-accent ring-2 ring-card"
            aria-hidden="true"
          />
        </button>

        <div ref={menuRef} className="relative">
          <button
            type="button"
            onClick={() => setMenuOpen((v) => !v)}
            aria-haspopup="menu"
            aria-expanded={menuOpen}
            className="flex items-center gap-2 rounded-md p-1 ps-1 pe-2 hover:bg-muted cursor-pointer"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-primary-soft text-sm font-semibold text-primary">
              {user ? initials(user.name) : '—'}
            </span>
            <span className="hidden flex-col items-start leading-tight sm:flex">
              <span className="text-sm font-medium text-foreground">
                {user?.name}
              </span>
              <span className="text-xs text-muted-foreground">
                {user ? roleLabels[user.role] : ''}
              </span>
            </span>
            <ChevronDown
              className={cn(
                'hidden h-4 w-4 text-muted-foreground transition-transform sm:block',
                menuOpen && 'rotate-180',
              )}
              aria-hidden="true"
            />
          </button>

          {menuOpen && (
            <div
              role="menu"
              className="absolute end-0 mt-2 w-60 overflow-hidden rounded-lg border border-border bg-card shadow-raised motion-safe:animate-[fadeIn_120ms_ease-out]"
            >
              <div className="border-b border-border px-4 py-3">
                <p className="text-sm font-semibold text-foreground">
                  {user?.name}
                </p>
                <p className="text-xs text-muted-foreground">{user?.email}</p>
                <p className="mt-1 text-xs text-muted-foreground">
                  {user ? roleLabels[user.role] : ''} · {user?.cooperative}
                </p>
              </div>
              <button
                type="button"
                role="menuitem"
                onClick={() => setMenuOpen(false)}
                className="flex w-full items-center gap-2.5 px-4 py-2.5 text-sm text-foreground hover:bg-muted cursor-pointer"
              >
                <UserRound className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
                My Profile
              </button>
              <button
                type="button"
                role="menuitem"
                onClick={handleLogout}
                className="flex w-full items-center gap-2.5 border-t border-border px-4 py-2.5 text-sm text-danger hover:bg-danger-soft cursor-pointer"
              >
                <LogOut className="h-4 w-4" aria-hidden="true" />
                Log out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
