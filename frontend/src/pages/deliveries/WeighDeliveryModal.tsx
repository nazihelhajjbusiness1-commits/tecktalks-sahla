import { useState } from 'react'
import { Modal, Button, Input, Alert } from '@/components/common'
import { deliveryService } from '@/services/deliveryService'
import { ApiError } from '@/services/api'

interface WeighDeliveryModalProps {
  deliveryId: number
  onClose: () => void
  onDone: () => void
}

const round3 = (n: number) => Math.round(n * 1000) / 1000

/**
 * Standalone weigh action for a PENDING delivery (Receiving's hand-off step),
 * reusing the same capture-weight endpoint as the intake wizard.
 */
export function WeighDeliveryModal({ deliveryId, onClose, onDone }: WeighDeliveryModalProps) {
  const [gross, setGross] = useState('')
  const [tare, setTare] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const grossNum = Number(gross)
  const tareNum = tare === '' ? 0 : Number(tare)
  const netPreview =
    gross !== '' &&
    !Number.isNaN(grossNum) &&
    !Number.isNaN(tareNum) &&
    grossNum > tareNum
      ? round3(grossNum - tareNum)
      : null

  async function submit() {
    setError(null)
    if (!(grossNum > 0)) {
      setError('Enter a valid gross weight.')
      return
    }
    if (tareNum < 0) {
      setError('Tare weight cannot be negative.')
      return
    }
    if (grossNum <= tareNum) {
      setError('Gross weight must be greater than tare weight.')
      return
    }
    setSubmitting(true)
    try {
      await deliveryService.captureWeight(deliveryId, {
        grossWeight: round3(grossNum),
        tareWeight: round3(tareNum),
      })
      onDone()
    } catch (err) {
      setError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'Could not record the weight.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open
      onClose={() => (submitting ? undefined : onClose())}
      title="Record weight"
      description="Capture gross and tare; the net weight is computed for you."
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button loading={submitting} onClick={submit}>
            Save weight
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
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Gross weight (kg)"
            type="number"
            required
            value={gross}
            onChange={(e) => setGross(e.target.value)}
          />
          <Input
            label="Tare weight (kg)"
            type="number"
            value={tare}
            onChange={(e) => setTare(e.target.value)}
            hint="Crates / container"
          />
        </div>
        <p className="text-sm text-muted-foreground">
          Net weight:{' '}
          <span className="font-medium text-foreground">
            {netPreview !== null ? `${netPreview} kg` : '—'}
          </span>
        </p>
      </div>
    </Modal>
  )
}
