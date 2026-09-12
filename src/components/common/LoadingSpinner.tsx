import { Loader2 } from 'lucide-react'
import { cn } from '@/utils/cn'

interface LoadingSpinnerProps {
  /** Visible + accessible label. */
  label?: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const sizes = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-8 w-8',
} as const

export function LoadingSpinner({
  label = 'Loading…',
  size = 'md',
  className,
}: LoadingSpinnerProps) {
  return (
    <div
      role="status"
      className={cn(
        'flex flex-col items-center gap-2 text-muted-foreground',
        className,
      )}
    >
      <Loader2 className={cn('animate-spin text-primary', sizes[size])} aria-hidden="true" />
      {label && <span className="text-sm">{label}</span>}
      <span className="sr-only">{label}</span>
    </div>
  )
}
