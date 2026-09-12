import type { LucideIcon } from 'lucide-react'
import { ArrowDownRight, ArrowUpRight } from 'lucide-react'
import { cn } from '@/utils/cn'

export type StatTone = 'primary' | 'accent' | 'success' | 'info' | 'neutral'

interface StatCardProps {
  label: string
  value: string
  icon: LucideIcon
  tone?: StatTone
  /** Optional trend, e.g. "+12% vs yesterday". */
  trend?: { value: string; direction: 'up' | 'down' | 'flat' }
  /** Optional secondary caption under the value. */
  caption?: string
}

const toneStyles: Record<StatTone, { iconWrap: string }> = {
  primary: { iconWrap: 'bg-primary-soft text-primary' },
  accent: { iconWrap: 'bg-accent-soft text-accent' },
  success: { iconWrap: 'bg-success-soft text-success' },
  info: { iconWrap: 'bg-info-soft text-info' },
  neutral: { iconWrap: 'bg-muted text-muted-foreground' },
}

export function StatCard({
  label,
  value,
  icon: Icon,
  tone = 'primary',
  trend,
  caption,
}: StatCardProps) {
  const TrendIcon =
    trend?.direction === 'down'
      ? ArrowDownRight
      : trend?.direction === 'up'
        ? ArrowUpRight
        : null

  return (
    <div className="rounded-lg border border-border bg-card p-5 shadow-card transition-shadow hover:shadow-raised">
      <div className="flex items-start justify-between gap-3">
        <p className="text-sm font-medium text-muted-foreground">{label}</p>
        <span
          className={cn(
            'flex h-9 w-9 shrink-0 items-center justify-center rounded-md',
            toneStyles[tone].iconWrap,
          )}
        >
          <Icon className="h-5 w-5" aria-hidden="true" />
        </span>
      </div>
      <p className="mt-2 font-heading text-2xl font-semibold tracking-tight text-foreground">
        {value}
      </p>
      {(trend || caption) && (
        <div className="mt-1.5 flex items-center gap-1.5 text-sm">
          {trend && (
            <span
              className={cn(
                'inline-flex items-center gap-0.5 font-medium',
                trend.direction === 'up' && 'text-success',
                trend.direction === 'down' && 'text-danger',
                trend.direction === 'flat' && 'text-muted-foreground',
              )}
            >
              {TrendIcon && <TrendIcon className="h-3.5 w-3.5" aria-hidden="true" />}
              {trend.value}
            </span>
          )}
          {caption && <span className="text-muted-foreground">{caption}</span>}
        </div>
      )}
    </div>
  )
}
