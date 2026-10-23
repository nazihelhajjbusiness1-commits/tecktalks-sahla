import { ClipboardCheck, AlertTriangle, Layers } from 'lucide-react'
import {
  PageHeader,
  StatCard,
  Card,
  CardHeader,
  Alert,
  EmptyState,
  LoadingSpinner,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { qualityService } from '@/services/qualityService'
import { cn } from '@/utils/cn'

/** Bar colour per grade — green for top grade down to red for rejects. */
function barColor(gradeCode: string): string {
  if (gradeCode === 'REJECT') return 'bg-danger'
  if (gradeCode === 'GRADE_A' || gradeCode === 'PREMIUM') return 'bg-success'
  if (gradeCode === 'GRADE_B') return 'bg-info'
  if (gradeCode === 'GRADE_C') return 'bg-accent'
  return 'bg-primary'
}

export function QualityPage() {
  const { data, loading, error } = useAsync(() => qualityService.summary(), [])

  const total = data?.totalGraded ?? 0
  const rejectPct = data ? (data.rejectRate * 100).toFixed(1) : '—'
  const gradesInUse = data?.distribution.length ?? 0
  const maxCount = Math.max(1, ...(data?.distribution.map((d) => d.count) ?? [0]))

  return (
    <div className="space-y-6">
      <PageHeader
        title="Quality control"
        description="Grade distribution and reject rate across all inspected deliveries."
      />

      {error && (
        <Alert variant="danger" title="Couldn't load quality data">
          {error}
        </Alert>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard
          label="Deliveries graded"
          value={loading ? '—' : String(total)}
          icon={ClipboardCheck}
          tone="primary"
        />
        <StatCard
          label="Reject rate"
          value={loading ? '—' : `${rejectPct}%`}
          icon={AlertTriangle}
          tone="accent"
          caption={data ? `${data.rejectCount} rejected of ${total}` : undefined}
        />
        <StatCard
          label="Grades in use"
          value={loading ? '—' : String(gradesInUse)}
          icon={Layers}
          tone="info"
        />
      </div>

      <Card>
        <CardHeader
          title="Grade distribution"
          description="How inspected produce breaks down by quality grade."
        />

        {loading ? (
          <div className="py-12">
            <LoadingSpinner />
          </div>
        ) : !data || data.distribution.length === 0 ? (
          <EmptyState
            icon={ClipboardCheck}
            title="No grades recorded yet"
            description="Grade distribution appears once deliveries have been inspected."
          />
        ) : (
          <ul className="mt-4 space-y-4">
            {data.distribution.map((row) => {
              const pctOfTotal = total ? Math.round((row.count / total) * 100) : 0
              return (
                <li key={row.gradeCode}>
                  <div className="mb-1 flex items-baseline justify-between text-sm">
                    <span className="font-medium text-foreground">
                      {row.gradeName}
                    </span>
                    <span className="text-muted-foreground">
                      {row.count} · {pctOfTotal}%
                    </span>
                  </div>
                  <div className="h-2.5 w-full overflow-hidden rounded-full bg-muted">
                    <div
                      className={cn('h-full rounded-full', barColor(row.gradeCode))}
                      style={{ width: `${(row.count / maxCount) * 100}%` }}
                    />
                  </div>
                </li>
              )
            })}
          </ul>
        )}
      </Card>
    </div>
  )
}
