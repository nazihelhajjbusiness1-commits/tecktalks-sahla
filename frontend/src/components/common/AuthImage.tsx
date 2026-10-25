import { useEffect, useState } from 'react'
import { ImageOff } from 'lucide-react'
import { cn } from '@/utils/cn'
import { LoadingSpinner } from './LoadingSpinner'

interface AuthImageProps {
  /** Resolves the image bytes to an object URL (e.g. an authenticated fetch). */
  loader: () => Promise<string>
  /** Changing this re-runs the loader (e.g. the photo id). */
  loaderKey: string | number
  alt: string
  className?: string
}

/**
 * Renders an image that sits behind JWT auth. A plain `<img src>` can't send
 * the Authorization header, so we fetch the bytes as a blob, show it via an
 * object URL, and revoke the URL on unmount/change to avoid leaking memory.
 */
export function AuthImage({ loader, loaderKey, alt, className }: AuthImageProps) {
  const [url, setUrl] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    let objectUrl: string | null = null
    setUrl(null)
    setFailed(false)

    loader()
      .then((resolved) => {
        if (!active) {
          URL.revokeObjectURL(resolved)
          return
        }
        objectUrl = resolved
        setUrl(resolved)
      })
      .catch(() => {
        if (active) setFailed(true)
      })

    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
    // loader is recreated each render; loaderKey is the real dependency.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loaderKey])

  if (failed) {
    return (
      <div
        className={cn(
          'flex items-center justify-center bg-muted text-muted-foreground',
          className,
        )}
      >
        <ImageOff className="h-5 w-5" aria-hidden="true" />
        <span className="sr-only">Image failed to load</span>
      </div>
    )
  }

  if (!url) {
    return (
      <div className={cn('flex items-center justify-center bg-muted', className)}>
        <LoadingSpinner size="sm" label="Loading image…" className="[&>span:not(.sr-only)]:hidden" />
      </div>
    )
  }

  return <img src={url} alt={alt} className={className} />
}
