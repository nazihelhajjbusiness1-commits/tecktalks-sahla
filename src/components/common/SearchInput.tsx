import { useId, type InputHTMLAttributes } from 'react'
import { Search, X } from 'lucide-react'
import { cn } from '@/utils/cn'

interface SearchInputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> {
  value: string
  onChange: (value: string) => void
  /** Accessible label (visually hidden). Defaults to "Search". */
  label?: string
  containerClassName?: string
}

/**
 * Controlled search field with a leading icon and a clear button.
 * Label is visually hidden but present for screen readers.
 */
export function SearchInput({
  value,
  onChange,
  label = 'Search',
  placeholder = 'Search…',
  className,
  containerClassName,
  id,
  ...props
}: SearchInputProps) {
  const autoId = useId()
  const inputId = id ?? autoId

  return (
    <div className={cn('relative', containerClassName)}>
      <label htmlFor={inputId} className="sr-only">
        {label}
      </label>
      <Search
        className="pointer-events-none absolute inset-y-0 start-3 my-auto h-4.5 w-4.5 text-muted-foreground"
        aria-hidden="true"
      />
      <input
        id={inputId}
        type="search"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className={cn(
          'h-11 w-full rounded-md border border-input bg-card ps-10 pe-10',
          'text-foreground placeholder:text-muted-foreground/70 transition-colors',
          'hover:border-muted-foreground/50',
          'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
          '[&::-webkit-search-cancel-button]:hidden',
          className,
        )}
        {...props}
      />
      {value && (
        <button
          type="button"
          onClick={() => onChange('')}
          aria-label="Clear search"
          className="absolute inset-y-0 end-2 my-auto flex h-7 w-7 items-center justify-center rounded text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer"
        >
          <X className="h-4 w-4" aria-hidden="true" />
        </button>
      )}
    </div>
  )
}
