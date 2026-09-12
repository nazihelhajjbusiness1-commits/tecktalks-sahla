import { useEffect, useState } from 'react'

interface AsyncState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

/**
 * Minimal data-fetching hook for the mock services. Runs the async factory
 * on mount (and when `deps` change) and tracks loading/error. Enough for
 * Week 1; swap for React Query later without touching call sites much.
 */
export function useAsync<T>(
  factory: () => Promise<T>,
  deps: React.DependencyList = [],
): AsyncState<T> {
  const [state, setState] = useState<AsyncState<T>>({
    data: null,
    loading: true,
    error: null,
  })

  // oxlint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    let active = true
    setState((s) => ({ ...s, loading: true, error: null }))
    factory()
      .then((data) => {
        if (active) setState({ data, loading: false, error: null })
      })
      .catch((err: unknown) => {
        if (active)
          setState({
            data: null,
            loading: false,
            error: err instanceof Error ? err.message : 'Failed to load data',
          })
      })
    return () => {
      active = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  return state
}
