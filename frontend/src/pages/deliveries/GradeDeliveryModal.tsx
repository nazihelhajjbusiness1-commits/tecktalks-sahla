import { useState } from 'react'
import { Modal, Button, Alert, LoadingSpinner } from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { deliveryService } from '@/services/deliveryService'
import { gradeService } from '@/services/gradeService'
import { ApiError } from '@/services/api'
import { cn } from '@/utils/cn'
import type { GradeDefinition } from '@/types'

interface GradeDeliveryModalProps {
  deliveryId: number
  productId: number | null
  onClose: () => void
  onDone: () => void
}

/**
 * Standalone grade action for a WEIGHED delivery (the Inspector's hand-off
 * step). Assigns a grade and, for non-rejected produce, confirms it into stock
 * — the same endpoints the intake wizard uses.
 */
export function GradeDeliveryModal({
  deliveryId,
  productId,
  onClose,
  onDone,
}: GradeDeliveryModalProps) {
  const { data, loading } = useAsync(
    () =>
      productId != null
        ? gradeService.listByProduct(productId)
        : Promise.resolve<GradeDefinition[]>([]),
    [productId],
  )
  const grades = (data ?? []).filter((g) => g.active)

  const [gradeCode, setGradeCode] = useState('')
  const [notes, setNotes] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit() {
    setError(null)
    if (!gradeCode) {
      setError('Select a grade to continue.')
      return
    }
    setSubmitting(true)
    try {
      const result = await deliveryService.captureGrade(deliveryId, {
        grade: gradeCode,
        notes: notes.trim() || undefined,
      })
      // Non-rejected produce is committed to inventory, mirroring intake.
      if (result.resultingStatus !== 'REJECTED') {
        await deliveryService.confirm(deliveryId)
      }
      onDone()
    } catch (err) {
      setError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'Could not save the grade.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open
      onClose={() => (submitting ? undefined : onClose())}
      title="Grade delivery"
      description="Assign a quality grade — the grade determines the price."
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button loading={submitting} onClick={submit} disabled={loading}>
            Save grade
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

        {loading ? (
          <div className="flex justify-center py-6">
            <LoadingSpinner label="Loading grades…" />
          </div>
        ) : grades.length === 0 ? (
          <Alert variant="warning" title="No grades configured">
            This product has no active grade definitions. Add them under
            Settings → Products before grading.
          </Alert>
        ) : (
          <fieldset disabled={submitting} className="space-y-2">
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
                  name="delivery-grade"
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
            htmlFor="detail-grade-notes"
            className="text-sm font-medium text-foreground"
          >
            Inspection notes{' '}
            <span className="font-normal text-muted-foreground">(optional)</span>
          </label>
          <textarea
            id="detail-grade-notes"
            rows={2}
            maxLength={500}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="e.g. minor bruising on ~10% of crates"
            className="mt-1 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground/70 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          />
        </div>
      </div>
    </Modal>
  )
}
