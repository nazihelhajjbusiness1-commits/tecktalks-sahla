import type { ButtonHTMLAttributes, ReactNode } from 'react'
import type { LucideIcon } from 'lucide-react'
import { Loader2 } from 'lucide-react'
import { cn } from '@/utils/cn'

export type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'outline'
  | 'ghost'
  | 'danger'
export type ButtonSize = 'sm' | 'md' | 'lg'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
  /** Optional leading icon. */
  icon?: LucideIcon
  /** Show a spinner and disable the button. */
  loading?: boolean
  /** Stretch to the container width. */
  block?: boolean
  children?: ReactNode
}

const base =
  'inline-flex items-center justify-center gap-2 rounded-md font-medium ' +
  'transition-colors duration-150 focus-visible:outline-2 ' +
  'focus-visible:outline-offset-2 focus-visible:outline-ring ' +
  'disabled:pointer-events-none disabled:opacity-50 select-none cursor-pointer'

const variants: Record<ButtonVariant, string> = {
  primary: 'bg-primary text-on-primary hover:bg-primary-hover shadow-card',
  secondary: 'bg-secondary text-on-secondary hover:brightness-95 shadow-card',
  outline:
    'border border-border bg-card text-foreground hover:bg-muted hover:border-input',
  ghost: 'text-foreground hover:bg-muted',
  danger: 'bg-danger text-white hover:brightness-95 shadow-card',
}

// Min-height keeps touch targets >= 44px on md/lg for tablet use.
const sizes: Record<ButtonSize, string> = {
  sm: 'h-9 px-3 text-sm',
  md: 'h-11 px-4 text-sm',
  lg: 'h-12 px-6 text-base',
}

export function Button({
  variant = 'primary',
  size = 'md',
  icon: Icon,
  loading = false,
  block = false,
  className,
  children,
  disabled,
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={cn(
        base,
        variants[variant],
        sizes[size],
        block && 'w-full',
        className,
      )}
      {...props}
    >
      {loading ? (
        <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
      ) : (
        Icon && <Icon className="h-4 w-4" aria-hidden="true" />
      )}
      {children}
    </button>
  )
}
