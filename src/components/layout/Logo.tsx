import { cn } from '@/utils/cn'
import { brand } from '@/config/brand'

interface LogoProps {
  /** Hide the wordmark, show only the leaf mark (collapsed sidebar). */
  markOnly?: boolean
  /** Render name/tagline in inverted colors (on the dark sidebar). */
  inverted?: boolean
  className?: string
}

/**
 * Brand lockup: an olive-leaf mark + wordmark, driven by `brand` config.
 * The mark is an inline SVG (a simple pair of olive leaves) so branding
 * can be swapped centrally without external assets.
 */
export function Logo({ markOnly = false, inverted = false, className }: LogoProps) {
  return (
    <div className={cn('flex items-center gap-2.5', className)}>
      <span
        className={cn(
          'flex h-9 w-9 shrink-0 items-center justify-center rounded-md',
          inverted ? 'bg-on-primary/10' : 'bg-primary',
        )}
      >
        <svg
          viewBox="0 0 24 24"
          className={cn('h-5 w-5', inverted ? 'text-on-primary' : 'text-on-primary')}
          fill="none"
          aria-hidden="true"
        >
          {/* Olive leaf / sprout mark */}
          <path
            d="M12 21c0-5 0-8 0-8"
            stroke="currentColor"
            strokeWidth="1.6"
            strokeLinecap="round"
          />
          <path
            d="M12 13c-3.5 0-6-2-6-5.5C9.5 7.5 12 9.5 12 13Z"
            fill="currentColor"
            opacity="0.55"
          />
          <path
            d="M12 11c0-3.5 2-6 5.5-6C17.5 8.5 15.5 11 12 11Z"
            fill="currentColor"
          />
        </svg>
      </span>
      {!markOnly && (
        <span className="flex flex-col leading-none">
          <span
            className={cn(
              'font-heading text-lg font-semibold tracking-tight',
              inverted ? 'text-on-primary' : 'text-foreground',
            )}
          >
            {brand.name}
          </span>
          <span
            className={cn(
              'mt-0.5 text-[11px]',
              inverted ? 'text-on-primary/70' : 'text-muted-foreground',
            )}
          >
            {brand.tagline}
          </span>
        </span>
      )}
    </div>
  )
}
