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

interface Notification {
  id: string
  title: string
  detail: string
  time: string
  unread: boolean
}

/**
 * Sample activity feed. Static for now — wire to a real `/api/notifications`
 * endpoint later; the popover UI stays the same.
 */
const notifications: Notification[] = [
  {
    id: 'n1',
    title: 'New delivery recorded',
    detail: 'Youssef Haddad · Apple A · 420 kg',
    time: '10m ago',
    unread: true,
  },
  {
    id: 'n2',
    title: 'Settlement awaiting confirmation',
    detail: 'Rima Khoury · $1,240.00',
    time: '1h ago',
    unread: true,
  },
  {
    id: 'n3',
    title: 'Payment recorded',
    detail: 'Charbel Sleiman · $500.00',
    time: '3h ago',
    unread: true,
  },
  {
    id: 'n4',
    title: 'Low inventory',
    detail: 'Orange A is below its reorder level',
    time: 'Yesterday',
    unread: false,
  },
]

export function Header({ title, onOpenMobileNav }: HeaderProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const [notifOpen, setNotifOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)
  const notifRef = useRef<HTMLDivElement>(null)

  const unreadCount = notifications.filter((n) => n.unread).length

  useEffect(() => {
    if (!menuOpen && !notifOpen) return
    const onClick = (e: MouseEvent) => {
      const target = e.target as Node
      if (menuRef.current && !menuRef.current.contains(target)) {
        setMenuOpen(false)
      }
      if (notifRef.current && !notifRef.current.contains(target)) {
        setNotifOpen(false)
      }
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setMenuOpen(false)
        setNotifOpen(false)
      }
    }
    document.addEventListener('mousedown', onClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [menuOpen, notifOpen])

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const openProfile = () => {
    setMenuOpen(false)
    navigate('/profile')
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
        <div ref={notifRef} className="relative">
          <button
            type="button"
            onClick={() => {
              setNotifOpen((v) => !v)
              setMenuOpen(false)
            }}
            aria-haspopup="menu"
            aria-expanded={notifOpen}
            className="relative flex h-10 w-10 items-center justify-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer"
            aria-label={`Notifications (${unreadCount} unread)`}
          >
            <Bell className="h-5 w-5" aria-hidden="true" />
            {unreadCount > 0 && (
              <span
                className="absolute end-2 top-2 h-2 w-2 rounded-full bg-accent ring-2 ring-card"
                aria-hidden="true"
              />
            )}
          </button>

          {notifOpen && (
            <div
              role="menu"
              className="absolute end-0 mt-2 w-80 overflow-hidden rounded-lg border border-border bg-card shadow-raised motion-safe:animate-[fadeIn_120ms_ease-out]"
            >
              <div className="flex items-center justify-between border-b border-border px-4 py-3">
                <p className="text-sm font-semibold text-foreground">
                  Notifications
                </p>
                {unreadCount > 0 && (
                  <span className="rounded-full bg-primary-soft px-2 py-0.5 text-xs font-medium text-primary">
                    {unreadCount} new
                  </span>
                )}
              </div>

              <ul className="max-h-96 overflow-y-auto">
                {notifications.map((n) => (
                  <li key={n.id}>
                    <div
                      className={cn(
                        'flex gap-3 border-b border-border px-4 py-3 last:border-0',
                        n.unread && 'bg-primary-soft/30',
                      )}
                    >
                      <span
                        className={cn(
                          'mt-1.5 h-2 w-2 shrink-0 rounded-full',
                          n.unread ? 'bg-accent' : 'bg-border',
                        )}
                        aria-hidden="true"
                      />
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-foreground">
                          {n.title}
                        </p>
                        <p className="truncate text-xs text-muted-foreground">
                          {n.detail}
                        </p>
                        <p className="mt-0.5 text-xs text-muted-foreground/70">
                          {n.time}
                        </p>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <div ref={menuRef} className="relative">
          <button
            type="button"
            onClick={() => {
              setMenuOpen((v) => !v)
              setNotifOpen(false)
            }}
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
                onClick={openProfile}
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
