import { forwardRef, useId, type InputHTMLAttributes, type ReactNode } from 'react'
import type { LucideIcon } from 'lucide-react'
import { cn } from '@/utils/cn'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  /** Visible label — always rendered (no placeholder-only inputs). */
  label?: string
  /** Small helper text under the field. */
  hint?: string
  /** Error message; also styles the field and sets aria-invalid. */
  error?: string
  /** Leading icon inside the field. */
  icon?: LucideIcon
  /** Trailing adornment (e.g. a show/hide password button). */
  trailing?: ReactNode
  containerClassName?: string
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  {
    label,
    hint,
    error,
    icon: Icon,
    trailing,
    id,
    className,
    containerClassName,
    required,
    ...props
  },
  ref,
) {
  const autoId = useId()
  const inputId = id ?? autoId
  const hintId = hint ? `${inputId}-hint` : undefined
  const errorId = error ? `${inputId}-error` : undefined

  return (
    <div className={cn('flex flex-col gap-1.5', containerClassName)}>
      {label && (
        <label
          htmlFor={inputId}
          className="text-sm font-medium text-foreground"
        >
          {label}
          {required && <span className="text-danger"> *</span>}
        </label>
      )}
      <div className="relative">
        {Icon && (
          <Icon
            className="pointer-events-none absolute inset-y-0 start-3 my-auto h-4.5 w-4.5 text-muted-foreground"
            aria-hidden="true"
          />
        )}
        <input
          ref={ref}
          id={inputId}
          required={required}
          aria-invalid={error ? true : undefined}
          aria-describedby={cn(errorId, hintId) || undefined}
          className={cn(
            'h-11 w-full rounded-md border bg-card text-foreground',
            'placeholder:text-muted-foreground/70 transition-colors',
            'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
            'disabled:cursor-not-allowed disabled:opacity-60',
            Icon ? 'ps-10' : 'ps-3.5',
            trailing ? 'pe-11' : 'pe-3.5',
            error
              ? 'border-danger focus-visible:outline-danger'
              : 'border-input hover:border-muted-foreground/50',
            className,
          )}
          {...props}
        />
        {trailing && (
          <div className="absolute inset-y-0 end-1.5 flex items-center">
            {trailing}
          </div>
        )}
      </div>
      {error ? (
        <p id={errorId} className="text-sm text-danger">
          {error}
        </p>
      ) : (
        hint && (
          <p id={hintId} className="text-sm text-muted-foreground">
            {hint}
          </p>
        )
      )}
    </div>
  )
})
