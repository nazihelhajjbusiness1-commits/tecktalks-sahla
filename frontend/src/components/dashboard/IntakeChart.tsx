import { useMemo, useId } from 'react'
import { LineChart } from 'lucide-react'
import { EmptyState } from '@/components/common'
import { formatWeight } from '@/utils/format'
import type { DashboardRecentDelivery } from '@/types'

interface IntakeChartProps {
  /** Recent deliveries; grouped by day to plot kg received. */
  deliveries: DashboardRecentDelivery[]
  loading?: boolean
}

interface DayPoint {
  date: string
  kg: number
}

function groupByDay(rows: DashboardRecentDelivery[]): DayPoint[] {
  const map = new Map<string, number>()
  for (const d of rows) {
    const day = d.deliveryDate?.slice(0, 10)
    if (!day) continue
    map.set(day, (map.get(day) ?? 0) + (d.quantity ?? 0))
  }
  return [...map.entries()]
    .map(([date, kg]) => ({ date, kg }))
    .sort((a, b) => a.date.localeCompare(b.date))
}

function shortDay(iso: string): string {
  const d = new Date(`${iso}T00:00:00`)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

const W = 640
const H = 200
const PAD_X = 32
const PAD_Y = 24

/**
 * Lightweight, dependency-free area chart of produce received per day.
 * Driven by `recentDeliveries` (the only day-level data the dashboard API
 * exposes today) — so it reflects the recent window, not a fixed range.
 */
export function IntakeChart({ deliveries, loading }: IntakeChartProps) {
  const gradientId = useId()
  const days = useMemo(() => groupByDay(deliveries), [deliveries])

  const max = Math.max(1, ...days.map((d) => d.kg))
  const points = days.map((d, i) => {
    const x =
      days.length === 1
        ? W / 2
        : PAD_X + (i * (W - 2 * PAD_X)) / (days.length - 1)
    const y = H - PAD_Y - (d.kg / max) * (H - 2 * PAD_Y)
    return { ...d, x, y }
  })

  const linePath = points
    .map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`)
    .join(' ')
  const baseline = H - PAD_Y
  const areaPath =
    points.length > 1
      ? `${linePath} L${points[points.length - 1].x.toFixed(1)},${baseline} L${points[0].x.toFixed(1)},${baseline} Z`
      : ''

  const total = days.reduce((sum, d) => sum + d.kg, 0)

  return (
    <div className="flex h-full flex-col rounded-lg border border-border bg-card shadow-card">
      <div className="flex flex-wrap items-start justify-between gap-3 border-b border-border px-5 py-4">
        <div>
          <h2 className="text-base font-semibold text-foreground">
            Produce Intake
          </h2>
          <p className="mt-0.5 text-sm text-muted-foreground">
            Received per day across recent deliveries
          </p>
        </div>
        {!loading && days.length > 0 && (
          <span className="rounded-full bg-primary-soft px-2.5 py-0.5 text-sm font-semibold text-primary">
            {formatWeight(total)} total
          </span>
        )}
      </div>

      <div className="flex-1 p-5">
        {loading ? (
          <p className="text-sm text-muted-foreground">Loading…</p>
        ) : days.length === 0 ? (
          <EmptyState
            icon={LineChart}
            title="No intake to chart yet"
            description="Daily produce received will plot here once deliveries are recorded."
          />
        ) : (
          <figure className="text-primary">
            <svg
              viewBox={`0 0 ${W} ${H}`}
              className="h-48 w-full"
              role="img"
              aria-label={`Produce received per day, ${formatWeight(total)} total across ${days.length} day${days.length === 1 ? '' : 's'}`}
              preserveAspectRatio="none"
            >
              <defs>
                <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="currentColor" stopOpacity="0.22" />
                  <stop offset="100%" stopColor="currentColor" stopOpacity="0" />
                </linearGradient>
              </defs>

              {/* baseline */}
              <line
                x1={PAD_X}
                y1={baseline}
                x2={W - PAD_X}
                y2={baseline}
                className="text-border"
                stroke="currentColor"
                strokeWidth="1"
              />

              {areaPath && <path d={areaPath} fill={`url(#${gradientId})`} />}
              <path
                d={linePath}
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                vectorEffect="non-scaling-stroke"
              />
              {points.map((p) => (
                <circle
                  key={p.date}
                  cx={p.x}
                  cy={p.y}
                  r="3.5"
                  fill="var(--color-card)"
                  stroke="currentColor"
                  strokeWidth="2"
                  vectorEffect="non-scaling-stroke"
                />
              ))}
            </svg>

            <figcaption className="mt-2 flex justify-between text-xs text-muted-foreground">
              {points.map((p) => (
                <span key={p.date}>{shortDay(p.date)}</span>
              ))}
            </figcaption>
          </figure>
        )}
      </div>
    </div>
  )
}
