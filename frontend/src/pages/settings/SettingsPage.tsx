import { useNavigate } from 'react-router-dom'
import {
  Users,
  ShieldCheck,
  Package,
  Star,
  DollarSign,
  Building2,
  ChevronRight,
  type LucideIcon,
} from 'lucide-react'
import { PageHeader, Card } from '@/components/common'
import { cn } from '@/utils/cn'
import { brand } from '@/config/brand'

interface SettingDef {
  key: string
  title: string
  description: string
  icon: LucideIcon
  /** Destination route; when omitted the card is a "coming soon" placeholder. */
  to?: string
}

const settings: SettingDef[] = [
  { key: 'products', title: 'Products', description: 'Crops, produce, grades and pricing.', icon: Package, to: '/settings/products' },
  { key: 'grades', title: 'Grades', description: 'Quality grades, managed per product.', icon: Star, to: '/settings/products' },
  { key: 'pricing', title: 'Pricing', description: 'Grade-based price lists per product.', icon: DollarSign, to: '/settings/products' },
  { key: 'users', title: 'Users', description: 'Manage staff accounts and access.', icon: Users },
  { key: 'roles', title: 'Roles & Permissions', description: 'Define what each role can do.', icon: ShieldCheck },
  { key: 'coop', title: 'Cooperative Settings', description: 'Organization details and preferences.', icon: Building2 },
]

export function SettingsPage() {
  const navigate = useNavigate()

  return (
    <div className="space-y-6">
      <PageHeader
        title="Settings"
        description={`Configure ${brand.name} for your cooperative`}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {settings.map(({ key, title, description, icon: Icon, to }) => {
          const interactive = Boolean(to)
          return (
            <Card
              key={key}
              role={interactive ? 'button' : undefined}
              tabIndex={interactive ? 0 : undefined}
              aria-disabled={interactive ? undefined : true}
              onClick={interactive ? () => navigate(to!) : undefined}
              onKeyDown={
                interactive
                  ? (e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        navigate(to!)
                      }
                    }
                  : undefined
              }
              className={cn(
                'group flex items-center gap-4 transition-colors',
                interactive
                  ? 'cursor-pointer hover:border-input hover:bg-muted/40 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring'
                  : 'opacity-60',
              )}
            >
              <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-md bg-primary-soft text-primary">
                <Icon className="h-5.5 w-5.5" aria-hidden="true" />
              </span>
              <div className="min-w-0 flex-1">
                <h2 className="font-heading text-base font-semibold text-foreground">
                  {title}
                </h2>
                <p className="mt-0.5 text-sm text-muted-foreground">
                  {description}
                </p>
              </div>
              {interactive ? (
                <ChevronRight
                  className="h-5 w-5 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5"
                  aria-hidden="true"
                />
              ) : (
                <span className="shrink-0 rounded-full bg-muted px-2 py-0.5 text-xs font-medium text-muted-foreground">
                  Soon
                </span>
              )}
            </Card>
          )
        })}
      </div>
    </div>
  )
}
