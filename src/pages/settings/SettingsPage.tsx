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
import { brand } from '@/config/brand'

interface SettingDef {
  key: string
  title: string
  description: string
  icon: LucideIcon
}

const settings: SettingDef[] = [
  { key: 'users', title: 'Users', description: 'Manage staff accounts and access.', icon: Users },
  { key: 'roles', title: 'Roles & Permissions', description: 'Define what each role can do.', icon: ShieldCheck },
  { key: 'products', title: 'Products', description: 'Crops and produce the center handles.', icon: Package },
  { key: 'grades', title: 'Grades', description: 'Quality grades used during inspection.', icon: Star },
  { key: 'pricing', title: 'Pricing', description: 'Price lists per product and grade.', icon: DollarSign },
  { key: 'coop', title: 'Cooperative Settings', description: 'Organization details and preferences.', icon: Building2 },
]

export function SettingsPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Settings"
        description={`Configure ${brand.name} for your cooperative`}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {settings.map(({ key, title, description, icon: Icon }) => (
          <Card
            key={key}
            role="button"
            tabIndex={0}
            className="group flex cursor-pointer items-center gap-4 transition-colors hover:border-input hover:bg-muted/40 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-md bg-primary-soft text-primary">
              <Icon className="h-5.5 w-5.5" aria-hidden="true" />
            </span>
            <div className="min-w-0 flex-1">
              <h2 className="font-heading text-base font-semibold text-foreground">
                {title}
              </h2>
              <p className="mt-0.5 text-sm text-muted-foreground">{description}</p>
            </div>
            <ChevronRight
              className="h-5 w-5 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5"
              aria-hidden="true"
            />
          </Card>
        ))}
      </div>
    </div>
  )
}
