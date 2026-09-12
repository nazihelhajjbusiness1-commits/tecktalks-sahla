import type { ReactNode } from 'react'
import { cn } from '@/utils/cn'
import { LoadingSpinner } from './LoadingSpinner'
import { EmptyState } from './EmptyState'

export interface Column<T> {
  /** Header label. */
  header: string
  /** Cell renderer for a row. */
  cell: (row: T) => ReactNode
  /** Optional alignment; defaults to start. */
  align?: 'start' | 'end' | 'center'
  /** Extra classes for the cell + header (e.g. width, hide on mobile). */
  className?: string
  /** Screen-reader-only header (for action columns). */
  srOnlyHeader?: boolean
}

interface TableProps<T> {
  columns: Column<T>[]
  rows: T[]
  /** Stable key for each row. */
  rowKey: (row: T) => string
  /** Called on row click — makes rows interactive (keyboard + hover). */
  onRowClick?: (row: T) => void
  loading?: boolean
  /** Shown when there are no rows and not loading. */
  empty?: ReactNode
  className?: string
}

const alignClass = {
  start: 'text-start',
  end: 'text-end',
  center: 'text-center',
} as const

/**
 * Generic, strongly-typed data table.
 * Horizontally scrolls on narrow screens (wrapper) rather than breaking
 * the layout, with row hover + optional keyboard-activatable rows.
 */
export function Table<T>({
  columns,
  rows,
  rowKey,
  onRowClick,
  loading = false,
  empty,
  className,
}: TableProps<T>) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <LoadingSpinner label="Loading…" />
      </div>
    )
  }

  if (rows.length === 0) {
    return (
      <div className="py-6">
        {empty ?? <EmptyState title="Nothing to show yet" />}
      </div>
    )
  }

  return (
    <div className={cn('w-full overflow-x-auto scrollbar-thin', className)}>
      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-border">
            {columns.map((col, i) => (
              <th
                key={i}
                scope="col"
                className={cn(
                  'whitespace-nowrap px-4 py-3 font-medium text-muted-foreground',
                  alignClass[col.align ?? 'start'],
                  col.className,
                )}
              >
                {col.srOnlyHeader ? (
                  <span className="sr-only">{col.header}</span>
                ) : (
                  col.header
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const interactive = Boolean(onRowClick)
            return (
              <tr
                key={rowKey(row)}
                onClick={interactive ? () => onRowClick?.(row) : undefined}
                onKeyDown={
                  interactive
                    ? (e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault()
                          onRowClick?.(row)
                        }
                      }
                    : undefined
                }
                tabIndex={interactive ? 0 : undefined}
                role={interactive ? 'button' : undefined}
                className={cn(
                  'border-b border-border/70 transition-colors last:border-0',
                  interactive &&
                    'cursor-pointer hover:bg-muted/60 focus-visible:bg-muted focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-ring',
                )}
              >
                {columns.map((col, i) => (
                  <td
                    key={i}
                    className={cn(
                      'px-4 py-3 text-foreground align-middle',
                      alignClass[col.align ?? 'start'],
                      col.className,
                    )}
                  >
                    {col.cell(row)}
                  </td>
                ))}
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
