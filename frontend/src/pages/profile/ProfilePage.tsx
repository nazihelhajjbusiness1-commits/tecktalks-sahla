import {
  Mail,
  Building2,
  ShieldCheck,
  UserRound,
  Hash,
  type LucideIcon,
} from 'lucide-react'
import { PageHeader, Card } from '@/components/common'
import { useAuth } from '@/hooks/useAuth'
import { roleLabels } from '@/config/roles'
import { initials } from '@/utils/format'

interface ProfileField {
  label: string
  value: string
  icon: LucideIcon
}

export function ProfilePage() {
  const { user } = useAuth()

  if (!user) return null

  const fields: ProfileField[] = [
    { label: 'Full name', value: user.name, icon: UserRound },
    { label: 'Email', value: user.email, icon: Mail },
    { label: 'Role', value: roleLabels[user.role], icon: ShieldCheck },
    { label: 'Cooperative', value: user.cooperative, icon: Building2 },
    { label: 'User ID', value: user.id, icon: Hash },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="My Profile"
        description="Your account details and role within the cooperative"
      />

      {/* Identity header */}
      <Card className="flex flex-col items-center gap-4 text-center sm:flex-row sm:text-start">
        <span className="flex h-20 w-20 shrink-0 items-center justify-center rounded-full bg-primary-soft text-2xl font-semibold text-primary">
          {initials(user.name)}
        </span>
        <div className="min-w-0">
          <h2 className="font-heading text-xl font-semibold text-foreground">
            {user.name}
          </h2>
          <p className="mt-0.5 text-sm text-muted-foreground">{user.email}</p>
          <span className="mt-2 inline-flex items-center gap-1.5 rounded-full bg-primary-soft px-2.5 py-0.5 text-xs font-medium text-primary">
            <ShieldCheck className="h-3.5 w-3.5" aria-hidden="true" />
            {roleLabels[user.role]}
          </span>
        </div>
      </Card>

      {/* Detail fields */}
      <Card flush>
        <dl className="divide-y divide-border">
          {fields.map(({ label, value, icon: Icon }) => (
            <div
              key={label}
              className="flex items-center gap-4 px-5 py-4"
            >
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground">
                <Icon className="h-5 w-5" aria-hidden="true" />
              </span>
              <div className="min-w-0">
                <dt className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                  {label}
                </dt>
                <dd className="mt-0.5 break-words text-sm font-medium text-foreground">
                  {value}
                </dd>
              </div>
            </div>
          ))}
        </dl>
      </Card>
    </div>
  )
}
