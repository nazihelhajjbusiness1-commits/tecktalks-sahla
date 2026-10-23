import { cn } from '@/utils/cn'
import { brand } from '@/config/brand'

interface LogoProps {
  /** Hide the wordmark, show only the brand mark (collapsed sidebar). */
  markOnly?: boolean
  /**
   * Render the complete lockup image (mark + wordmark + tagline).
   * Use on light surfaces where there's room — e.g. the login screen.
   * Ignores `markOnly`/`inverted`.
   */
  full?: boolean
  /** Render the wordmark in inverted colors (on the dark sidebar/panel). */
  inverted?: boolean
  className?: string
}

/**
 * Brand lockup. The mark is the real Sahla icon (served from /public), shown
 * on a white chip so it keeps contrast on both the cream workspace and the
 * deep-green sidebar. Naming/tagline come from the `brand` config.
 */
export function Logo({
  markOnly = false,
  full = false,
  inverted = false,
  className,
}: LogoProps) {
  if (full) {
    return (
      <img
        src="/sahla-logo.png"
        alt={`${brand.name} — ${brand.tagline}`}
        className={cn('block max-w-full select-none', className)}
        width={1074}
        height={728}
      />
    )
  }

  return (
    <div className={cn('flex items-center gap-3', className)}>
      <span className="flex h-11 w-11 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-white p-1 shadow-card ring-1 ring-black/5">
        <img
          src="/sahla-mark.png"
          alt=""
          aria-hidden="true"
          className="h-full w-full object-contain"
          width={340}
          height={340}
        />
      </span>
      {!markOnly && (
        <span className="flex flex-col leading-none">
          <span
            className={cn(
              'font-heading text-xl font-bold tracking-tight',
              inverted ? 'text-on-primary' : 'text-foreground',
            )}
          >
            {brand.name}
          </span>
          <span
            className={cn(
              'mt-1 text-[11px] font-medium',
              inverted ? 'text-on-primary/65' : 'text-muted-foreground',
            )}
          >
            {brand.tagline}
          </span>
        </span>
      )}
    </div>
  )
}
