import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Gavel, Check, X, ArrowRight } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  Alert,
  EmptyState,
  LoadingSpinner,
  Modal,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { gradeSuggestionService } from '@/services/gradeSuggestionService'
import { ApiError } from '@/services/api'
import { formatDateTime } from '@/utils/format'
import { DeliveryPhotos } from '@/pages/deliveries/DeliveryPhotos'
import type { GradeSuggestionResponse } from '@/types'

/**
 * Manager's grade-approval queue. Lists PENDING suggestions raised by inspectors
 * — current vs suggested grade, the inspector's reason, and the produce photos —
 * and lets the manager approve (overriding the grade) or reject each one.
 */
export function GradeReviewsPage() {
  const [reloadKey, setReloadKey] = useState(0)
  const { data, loading, error } = useAsync(
    () => gradeSuggestionService.listPending(),
    [reloadKey],
  )
  const suggestions = data ?? []

  const [busyId, setBusyId] = useState<number | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [rejecting, setRejecting] = useState<GradeSuggestionResponse | null>(null)
  const [rejectNote, setRejectNote] = useState('')

  const reload = () => setReloadKey((k) => k + 1)

  async function approve(s: GradeSuggestionResponse) {
    setActionError(null)
    setBusyId(s.id)
    try {
      await gradeSuggestionService.approve(s.id)
      reload()
    } catch (err) {
      setActionError(errMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  async function confirmReject() {
    if (!rejecting) return
    setActionError(null)
    setBusyId(rejecting.id)
    try {
      await gradeSuggestionService.reject(rejecting.id, rejectNote)
      setRejecting(null)
      setRejectNote('')
      reload()
    } catch (err) {
      setActionError(errMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Grade reviews"
        description="Inspector-proposed grade changes awaiting your approval. Approving overrides the delivery's grade and recomputes its price."
      />

      {actionError && (
        <Alert variant="danger" onDismiss={() => setActionError(null)}>
          {actionError}
        </Alert>
      )}
      {error && (
        <Alert variant="danger" title="Couldn't load grade reviews">
          {error}
        </Alert>
      )}

      {loading ? (
        <div className="py-12">
          <LoadingSpinner label="Loading suggestions…" />
        </div>
      ) : suggestions.length === 0 ? (
        <EmptyState
          icon={Gavel}
          title="Nothing to review"
          description="When an inspector suggests a different grade, it shows up here for approval."
        />
      ) : (
        <ul className="space-y-4">
          {suggestions.map((s) => (
            <li key={s.id}>
              <Card>
                <div className="flex flex-col gap-4">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <Link
                        to={`/deliveries/${s.deliveryId}`}
                        className="font-heading text-base font-semibold text-foreground hover:underline"
                      >
                        Delivery #{s.deliveryId}
                      </Link>
                      <p className="mt-0.5 text-sm text-muted-foreground">
                        {[s.farmerName, s.productName].filter(Boolean).join(' · ')}
                      </p>
                    </div>
                    <div className="flex items-center gap-2 text-sm">
                      <span className="rounded-md bg-muted px-2 py-1 font-medium text-muted-foreground">
                        {s.currentGradeName ?? s.currentGradeCode ?? 'current'}
                      </span>
                      <ArrowRight className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
                      <span className="rounded-md bg-primary-soft/50 px-2 py-1 font-semibold text-foreground">
                        {s.suggestedGradeName}{' '}
                        <span className="font-mono text-xs text-muted-foreground">
                          {s.suggestedGradeCode}
                        </span>
                      </span>
                    </div>
                  </div>

                  <div className="rounded-md bg-muted px-3 py-2 text-sm text-foreground">
                    <span className="font-medium">Inspector's reason:</span> {s.reason}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Suggested by {s.suggestedBy ?? 'inspector'} · {formatDateTime(s.createdAt)}
                  </p>

                  <DeliveryPhotos deliveryId={s.deliveryId} canUpload={false} compact />

                  <div className="flex items-center justify-end gap-2">
                    <Button
                      variant="outline"
                      icon={X}
                      disabled={busyId === s.id}
                      onClick={() => {
                        setRejectNote('')
                        setRejecting(s)
                      }}
                    >
                      Reject
                    </Button>
                    <Button
                      icon={Check}
                      loading={busyId === s.id}
                      onClick={() => approve(s)}
                    >
                      Approve &amp; override
                    </Button>
                  </div>
                </div>
              </Card>
            </li>
          ))}
        </ul>
      )}

      {rejecting && (
        <Modal
          open
          onClose={() => (busyId ? undefined : setRejecting(null))}
          title={`Reject suggestion for delivery #${rejecting.deliveryId}`}
          description="The grade stays as it is. Optionally tell the inspector why."
          footer={
            <>
              <Button
                variant="outline"
                onClick={() => setRejecting(null)}
                disabled={busyId === rejecting.id}
              >
                Cancel
              </Button>
              <Button
                variant="danger"
                loading={busyId === rejecting.id}
                onClick={confirmReject}
              >
                Reject suggestion
              </Button>
            </>
          }
        >
          <textarea
            rows={3}
            maxLength={1000}
            value={rejectNote}
            onChange={(e) => setRejectNote(e.target.value)}
            placeholder="e.g. photos support the original Grade A"
            className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground/70 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          />
        </Modal>
      )}
    </div>
  )
}

function errMessage(err: unknown): string {
  return err instanceof ApiError || err instanceof Error ? err.message : 'Action failed.'
}
