import type { ReactNode } from 'react'
import type { LucideIcon } from 'lucide-react'
import {
  CheckCircle2,
  Info,
  AlertTriangle,
  XCircle,
  X,
} from 'lucide-react'
import { cn } from '@/utils/cn'

export type AlertVariant = 'info' | 'success' | 'warning' | 'danger'

interface AlertProps {
  variant?: AlertVariant
  title?: string
  children?: ReactNode
  /** Renders a dismiss button that calls this handler. */
  onDismiss?: () => void
  className?: string
}

const config: Record<
  AlertVariant,
  { wrap: string; icon: LucideIcon; iconColor: string; role: 'alert' | 'status' }
> = {
  info: {
    wrap: 'bg-info-soft border-info/30 text-foreground',
    icon: Info,
    iconColor: 'text-info',
    role: 'status',
  },
  success: {
    wrap: 'bg-success-soft border-success/30 text-foreground',
    icon: CheckCircle2,
    iconColor: 'text-success',
    role: 'status',
  },
  warning: {
    wrap: 'bg-warning-soft border-warning/30 text-foreground',
    icon: AlertTriangle,
    iconColor: 'text-warning',
    role: 'alert',
  },
  danger: {
    wrap: 'bg-danger-soft border-danger/30 text-foreground',
    icon: XCircle,
    iconColor: 'text-danger',
    role: 'alert',
  },
}

export function Alert({
  variant = 'info',
  title,
  children,
  onDismiss,
  className,
}: AlertProps) {
  const { wrap, icon: Icon, iconColor, role } = config[variant]
  return (
    <div
      role={role}
      className={cn(
        'flex items-start gap-3 rounded-md border px-4 py-3 text-sm',
        wrap,
        className,
      )}
    >
      <Icon className={cn('mt-0.5 h-5 w-5 shrink-0', iconColor)} aria-hidden="true" />
      <div className="min-w-0 flex-1">
        {title && <p className="font-semibold">{title}</p>}
        {children && (
          <div className={cn(title && 'mt-0.5', 'text-foreground/90')}>
            {children}
          </div>
        )}
      </div>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Dismiss"
          className="-me-1 flex h-6 w-6 items-center justify-center rounded text-muted-foreground hover:bg-black/5 hover:text-foreground cursor-pointer"
        >
          <X className="h-4 w-4" aria-hidden="true" />
        </button>
      )}
    </div>
  )
}
