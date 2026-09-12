import { forwardRef, useId, type SelectHTMLAttributes } from 'react'
import { ChevronDown } from 'lucide-react'
import { cn } from '@/utils/cn'

export interface SelectOption {
  label: string
  value: string
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  hint?: string
  error?: string
  options: SelectOption[]
  /** Placeholder shown as a disabled first option. */
  placeholder?: string
  containerClassName?: string
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  {
    label,
    hint,
    error,
    options,
    placeholder,
    id,
    className,
    containerClassName,
    required,
    value,
    defaultValue,
    ...props
  },
  ref,
) {
  const autoId = useId()
  const selectId = id ?? autoId

  return (
    <div className={cn('flex flex-col gap-1.5', containerClassName)}>
      {label && (
        <label
          htmlFor={selectId}
          className="text-sm font-medium text-foreground"
        >
          {label}
          {required && <span className="text-danger"> *</span>}
        </label>
      )}
      <div className="relative">
        <select
          ref={ref}
          id={selectId}
          required={required}
          aria-invalid={error ? true : undefined}
          value={value}
          defaultValue={defaultValue ?? (placeholder ? '' : undefined)}
          className={cn(
            'h-11 w-full appearance-none rounded-md border bg-card ps-3.5 pe-10',
            'text-foreground transition-colors cursor-pointer',
            'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
            'disabled:cursor-not-allowed disabled:opacity-60',
            error
              ? 'border-danger'
              : 'border-input hover:border-muted-foreground/50',
            className,
          )}
          {...props}
        >
          {placeholder && (
            <option value="" disabled>
              {placeholder}
            </option>
          )}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <ChevronDown
          className="pointer-events-none absolute inset-y-0 end-3 my-auto h-4.5 w-4.5 text-muted-foreground"
          aria-hidden="true"
        />
      </div>
      {error ? (
        <p className="text-sm text-danger">{error}</p>
      ) : (
        hint && <p className="text-sm text-muted-foreground">{hint}</p>
      )}
    </div>
  )
})
