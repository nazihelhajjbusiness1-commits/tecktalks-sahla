import { useMemo, useState } from 'react'
import { Modal, Button, Input, Select, Alert } from '@/components/common'
import { paymentService } from '@/services/paymentService'
import { ApiError } from '@/services/api'
import { formatMoney } from '@/utils/format'
import type { Currency, CurrencyBalance, PaymentMethod } from '@/types'

const methodOptions: { label: string; value: PaymentMethod }[] = [
  { label: 'Cash', value: 'CASH' },
  { label: 'Bank transfer', value: 'BANK_TRANSFER' },
  { label: 'Check', value: 'CHECK' },
  { label: 'Other', value: 'OTHER' },
]

interface RecordPaymentModalProps {
  open: boolean
  farmerId: number
  farmerName: string
  /** Outstanding balances for the farmer (one per currency). */
  balances: CurrencyBalance[]
  onClose: () => void
  onRecorded: () => void
}

export function RecordPaymentModal({
  open,
  farmerId,
  farmerName,
  balances,
  onClose,
  onRecorded,
}: RecordPaymentModalProps) {
  const payable = useMemo(() => balances.filter((b) => b.outstanding > 0), [balances])
  const [currency, setCurrency] = useState<Currency>(payable[0]?.currency ?? 'USD')
  const [amount, setAmount] = useState('')
  const [method, setMethod] = useState<PaymentMethod>('CASH')
  const [reference, setReference] = useState('')
  const [notes, setNotes] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const currentBalance = payable.find((b) => b.currency === currency)?.outstanding ?? 0

  async function submit() {
    setError(null)
    const value = Number(amount)
    if (Number.isNaN(value) || value <= 0) {
      setError('Enter a payment amount greater than zero.')
      return
    }
    if (value > currentBalance) {
      setError(`Amount exceeds the outstanding balance of ${formatMoney(currentBalance, currency)}.`)
      return
    }
    setSubmitting(true)
    try {
      await paymentService.recordPayment({
        farmerId,
        amount: value,
        currency,
        paymentMethod: method,
        referenceNumber: reference.trim() || undefined,
        notes: notes.trim() || undefined,
      })
      onRecorded()
    } catch (err) {
      setError(err instanceof ApiError || err instanceof Error ? err.message : 'Payment failed.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={() => (submitting ? undefined : onClose())}
      title={`Record payment — ${farmerName}`}
      description="Record a full or partial payment. The backend validates the balance."
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button loading={submitting} disabled={payable.length === 0} onClick={submit}>
            Record payment
          </Button>
        </>
      }
    >
      {payable.length === 0 ? (
        <Alert variant="info">This farmer has no outstanding balance to pay.</Alert>
      ) : (
        <div className="space-y-4">
          {error && (
            <Alert variant="danger" onDismiss={() => setError(null)}>
              {error}
            </Alert>
          )}

          <Select
            label="Currency"
            value={currency}
            options={payable.map((b) => ({ label: b.currency, value: b.currency }))}
            onChange={(e) => {
              setCurrency(e.target.value as Currency)
              setAmount('')
            }}
          />

          <div className="flex items-end gap-2">
            <Input
              label="Amount"
              type="number"
              min="0"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              hint={`Outstanding: ${formatMoney(currentBalance, currency)}`}
              containerClassName="flex-1"
            />
            <Button
              variant="outline"
              type="button"
              onClick={() => setAmount(String(currentBalance))}
            >
              Pay full
            </Button>
          </div>

          <Select
            label="Payment method"
            value={method}
            options={methodOptions}
            onChange={(e) => setMethod(e.target.value as PaymentMethod)}
          />

          <Input
            label="Reference number (optional)"
            value={reference}
            onChange={(e) => setReference(e.target.value)}
          />

          <div className="flex flex-col gap-1.5">
            <label htmlFor="payment-notes" className="text-sm font-medium text-foreground">
              Notes (optional)
            </label>
            <textarea
              id="payment-notes"
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full rounded-md border border-input bg-card px-3.5 py-2.5 text-foreground placeholder:text-muted-foreground/70 transition-colors hover:border-muted-foreground/50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            />
          </div>
        </div>
      )}
    </Modal>
  )
}
