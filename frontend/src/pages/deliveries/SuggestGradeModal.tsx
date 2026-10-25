import { useState } from 'react'
import { Modal, Button, Alert, LoadingSpinner } from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { gradeService } from '@/services/gradeService'
import { gradeSuggestionService } from '@/services/gradeSuggestionService'
import { ApiError } from '@/services/api'
import { cn } from '@/utils/cn'
import { DeliveryPhotos } from './DeliveryPhotos'
import type { GradeDefinition } from '@/types'

interface SuggestGradeModalProps {
  deliveryId: number
  productId: number | null
  /** The delivery's current grade code, excluded from the choices. */
  currentGradeCode: string | null
  onClose: () => void
  onDone: () => void
}

/**
 * Inspector raises a second-opinion grade suggestion: pick a different grade and
 * explain why (from the photos / their expertise). It goes to the manager's
 * approval queue — it does not change the grade directly.
 */
export function SuggestGradeModal({
  deliveryId,
  productId,
  currentGradeCode,
  onClose,
  onDone,
}: SuggestGradeModalProps) {
  const { data, loading } = useAsync(
    () =>
      productId != null
        ? gradeService.listByProduct(productId)
        : Promise.resolve<GradeDefinition[]>([]),
    [productId],
  )
  // Can't suggest the grade it already has.
  const grades = (data ?? []).filter((g) => g.active && g.gradeCode !== currentGradeCode)

  const [gradeCode, setGradeCode] = useState('')
  const [reason, setReason] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit() {
    setError(null)
    if (!gradeCode) {
      setError('Select the grade you think it should be.')
      return
    }
    if (!reason.trim()) {
      setError('Add a short reason for the change.')
      return
    }
    setSubmitting(true)
    try {
      await gradeSuggestionService.create(deliveryId, {
        suggestedGrade: gradeCode,
        reason: reason.trim(),
      })
      onDone()
    } catch (err) {
      setError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'Could not submit the suggestion.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open
      size="lg"
      onClose={() => (submitting ? undefined : onClose())}
      title="Suggest a different grade"
      description="Propose a grade change for a manager to approve. This doesn't change the grade yet."
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button loading={submitting} onClick={submit} disabled={loading}>
            Submit suggestion
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        {error && (
          <Alert variant="danger" onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Show the produce photos so the suggestion is grounded in the evidence. */}
        <DeliveryPhotos deliveryId={deliveryId} canUpload={false} compact />

        {loading ? (
          <div className="flex justify-center py-6">
            <LoadingSpinner label="Loading grades…" />
          </div>
        ) : grades.length === 0 ? (
          <Alert variant="warning" title="No other grades available">
            There are no other active grades configured for this product to suggest.
          </Alert>
        ) : (
          <fieldset disabled={submitting} className="space-y-2">
            <legend className="mb-1 text-sm font-medium text-foreground">
              Suggested grade
            </legend>
            {grades.map((g) => (
              <label
                key={g.id}
                className={cn(
                  'flex cursor-pointer items-start gap-3 rounded-md border p-3 transition-colors',
                  gradeCode === g.gradeCode
                    ? 'border-primary bg-primary-soft/40'
                    : 'border-border hover:border-muted-foreground/50',
                )}
              >
                <input
                  type="radio"
                  name="suggest-grade"
                  value={g.gradeCode}
                  checked={gradeCode === g.gradeCode}
                  onChange={() => setGradeCode(g.gradeCode)}
                  className="mt-1"
                />
                <span>
                  <span className="font-medium text-foreground">{g.name}</span>
                  <span className="ms-2 font-mono text-xs text-muted-foreground">
                    {g.gradeCode}
                  </span>
                  {g.description && (
                    <span className="block text-sm text-muted-foreground">
                      {g.description}
                    </span>
                  )}
                </span>
              </label>
            ))}
          </fieldset>
        )}

        <div>
          <label
            htmlFor="suggest-reason"
            className="text-sm font-medium text-foreground"
          >
            Reason
          </label>
          <textarea
            id="suggest-reason"
            rows={3}
            maxLength={1000}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="e.g. photos show bruising on ~20% of crates, should be Grade B, not A"
            className="mt-1 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground/70 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          />
        </div>
      </div>
    </Modal>
  )
}
