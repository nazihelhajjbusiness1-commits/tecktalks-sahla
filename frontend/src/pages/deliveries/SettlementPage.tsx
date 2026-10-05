import { useMemo, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { ArrowLeft, Plus, Trash2, Calculator, ShieldCheck } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  Input,
  Select,
  Alert,
  LoadingSpinner,
  Modal,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/hooks/useAuth'
import { deliveryService } from '@/services/deliveryService'
import { settlementService } from '@/services/settlementService'
import { paymentService } from '@/services/paymentService'
import { ApiError } from '@/services/api'
import { formatMoney, formatNumber, formatDateTime } from '@/utils/format'
import type {
  Currency,
  DeductionLine,
  DeductionType,
  FarmerBalances,
  FarmerSettlement,
  SettlementCalculation,
} from '@/types'

const deductionTypes: { label: string; value: DeductionType }[] = [
  { label: 'Transport', value: 'TRANSPORT' },
  { label: 'Packaging', value: 'PACKAGING' },
  { label: 'Service fee', value: 'SERVICE_FEE' },
  { label: 'Other', value: 'OTHER' },
]

interface DeductionRow {
  type: DeductionType
  description: string
  amount: string
}

function errorMessage(err: unknown): string {
  if (err instanceof ApiError || err instanceof Error) return err.message
  return 'Something went wrong. Please try again.'
}

export function SettlementPage() {
  const { id = '' } = useParams()
  const deliveryId = Number(id)
  const navigate = useNavigate()
  const { user } = useAuth()
  const canConfirm = ['admin', 'manager', 'accountant'].includes(user?.role ?? '')

  const { data, loading, error } = useAsync(async () => {
    const delivery = await deliveryService.getById(deliveryId)
    const [preview, settlement] = await Promise.all([
      settlementService.getPreview(deliveryId).catch((e: unknown) => {
        // A missing price rule is an actionable business error, not a crash.
        return { error: errorMessage(e) } as const
      }),
      settlementService.getByDelivery(deliveryId).catch(() => null),
    ])
    return { delivery, preview, settlement }
  }, [deliveryId])

  const [commissionRate, setCommissionRate] = useState('0')
  const [deductions, setDeductions] = useState<DeductionRow[]>([])
  const [calc, setCalc] = useState<SettlementCalculation | null>(null)
  const [confirmed, setConfirmed] = useState<FarmerSettlement | null>(null)
  const [balances, setBalances] = useState<FarmerBalances | null>(null)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  const delivery = data?.delivery
  const preview = data?.preview && !('error' in data.preview) ? data.preview : null
  const previewError = data?.preview && 'error' in data.preview ? data.preview.error : null
  const existing = confirmed ?? data?.settlement ?? null

  // Breakdown to display comes from a fresh calculation or an existing settlement.
  const breakdown = useMemo(() => {
    if (calc) {
      return {
        currency: calc.currency,
        unitPrice: calc.unitPrice,
        acceptedWeight: calc.acceptedWeight,
        grossAmount: calc.grossAmount,
        commissionAmount: calc.commissionAmount,
        deductionsTotal: calc.deductionsTotal,
        netAmount: calc.netAmount,
      }
    }
    if (existing && existing.status !== 'DRAFT' && existing.currency) {
      return {
        currency: existing.currency,
        unitPrice: existing.unitPriceSnapshot ?? 0,
        acceptedWeight: existing.acceptedWeightSnapshot ?? 0,
        grossAmount: existing.grossAmount ?? 0,
        commissionAmount: existing.commissionAmount ?? 0,
        deductionsTotal: existing.deductionsTotal ?? 0,
        netAmount: existing.netAmount ?? 0,
      }
    }
    return null
  }, [calc, existing])

  const isConfirmed = existing?.status === 'CONFIRMED'
  const currency: Currency = (preview?.currency ?? breakdown?.currency ?? 'USD') as Currency

  async function handleCalculate() {
    setActionError(null)
    if (!preview?.currency) {
      setActionError('Cannot calculate without a resolved price.')
      return
    }
    const rate = Number(commissionRate)
    if (Number.isNaN(rate) || rate < 0) {
      setActionError('Commission rate must be zero or greater.')
      return
    }
    const lines: DeductionLine[] = []
    for (const row of deductions) {
      const amount = Number(row.amount)
      if (!row.description.trim()) {
        setActionError('Each deduction needs a description.')
        return
      }
      if (Number.isNaN(amount) || amount < 0) {
        setActionError('Each deduction amount must be zero or greater.')
        return
      }
      lines.push({ type: row.type, description: row.description.trim(), amount })
    }
    setSubmitting(true)
    try {
      const result = await settlementService.calculate(deliveryId, {
        currency: preview.currency,
        commissionRate: rate,
        deductions: lines,
      })
      setCalc(result)
    } catch (err) {
      setActionError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleConfirm() {
    setActionError(null)
    setSubmitting(true)
    try {
      const result = await settlementService.confirm(deliveryId)
      setConfirmed(result)
      setConfirmOpen(false)
      // Balance must come from the backend ledger, not estimated locally.
      setBalances(await paymentService.getFarmerBalances(result.farmerId).catch(() => null))
    } catch (err) {
      setActionError(errorMessage(err))
      setConfirmOpen(false)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner label="Loading settlement…" />
      </div>
    )
  }

  if (error || !delivery) {
    return (
      <Alert variant="danger" title="Couldn't load settlement">
        {error ?? 'Delivery not found.'}
      </Alert>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-1 text-sm text-muted-foreground">
        <Link to="/deliveries" className="hover:text-foreground hover:underline">Deliveries</Link>
        <span aria-hidden="true">/</span>
        <Link to={`/deliveries/${deliveryId}`} className="hover:text-foreground hover:underline">
          #{deliveryId}
        </Link>
        <span aria-hidden="true">/</span>
        <span className="text-foreground">Settlement</span>
      </div>

      <PageHeader
        title={`Settlement — Delivery #${deliveryId}`}
        description={`${delivery.farmerName} · ${delivery.productName}`}
        actions={
          <Button variant="outline" icon={ArrowLeft} onClick={() => navigate(`/deliveries/${deliveryId}`)}>
            Back to delivery
          </Button>
        }
      />

      {actionError && (
        <Alert variant="danger" title="Action failed" onDismiss={() => setActionError(null)}>
          {actionError}
        </Alert>
      )}

      {delivery.status !== 'CONFIRMED' && !isConfirmed && (
        <Alert variant="warning" title="Delivery not confirmed">
          A settlement can only be calculated for a confirmed delivery. Current status:{' '}
          {delivery.status}.
        </Alert>
      )}

      {previewError && !isConfirmed && (
        <Alert variant="warning" title="Price not available">
          {previewError} Configure a grade-specific price rule for this product before
          calculating the settlement.
        </Alert>
      )}

      {/* Price / weight summary */}
      {(preview || breakdown) && (
        <Card>
          <h2 className="font-heading text-base font-semibold text-foreground">Delivery basis</h2>
          <dl className="mt-3 grid grid-cols-2 gap-x-6 gap-y-3 sm:grid-cols-4">
            <Fact label="Grade" value={preview?.grade ?? '—'} />
            <Fact
              label="Accepted weight"
              value={`${formatNumber(preview?.acceptedWeight ?? breakdown?.acceptedWeight ?? 0)} kg`}
            />
            <Fact
              label="Unit price"
              value={formatMoney(preview?.unitPrice ?? breakdown?.unitPrice ?? 0, currency)}
            />
            <Fact label="Currency" value={currency} />
          </dl>
        </Card>
      )}

      {/* Calculate form — only before a settlement exists / is calculated */}
      {!isConfirmed && !breakdown && delivery.status === 'CONFIRMED' && preview && (
        <Card>
          <h2 className="font-heading text-base font-semibold text-foreground">Calculate settlement</h2>
          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Commission rate (%)"
              type="number"
              min="0"
              step="0.01"
              value={commissionRate}
              onChange={(e) => setCommissionRate(e.target.value)}
            />
          </div>

          <div className="mt-5">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-foreground">Deductions</h3>
              <Button
                variant="outline"
                size="sm"
                icon={Plus}
                onClick={() =>
                  setDeductions((d) => [...d, { type: 'TRANSPORT', description: '', amount: '' }])
                }
              >
                Add deduction
              </Button>
            </div>
            {deductions.length === 0 ? (
              <p className="mt-2 text-sm text-muted-foreground">No deductions.</p>
            ) : (
              <div className="mt-3 space-y-3">
                {deductions.map((row, i) => (
                  <div key={i} className="grid grid-cols-1 gap-2 sm:grid-cols-[10rem_1fr_8rem_auto]">
                    <Select
                      aria-label="Deduction type"
                      value={row.type}
                      options={deductionTypes}
                      onChange={(e) =>
                        setDeductions((d) =>
                          d.map((r, j) => (j === i ? { ...r, type: e.target.value as DeductionType } : r)),
                        )
                      }
                    />
                    <Input
                      aria-label="Deduction description"
                      placeholder="Description"
                      value={row.description}
                      onChange={(e) =>
                        setDeductions((d) =>
                          d.map((r, j) => (j === i ? { ...r, description: e.target.value } : r)),
                        )
                      }
                    />
                    <Input
                      aria-label="Deduction amount"
                      type="number"
                      min="0"
                      step="0.01"
                      placeholder="Amount"
                      value={row.amount}
                      onChange={(e) =>
                        setDeductions((d) =>
                          d.map((r, j) => (j === i ? { ...r, amount: e.target.value } : r)),
                        )
                      }
                    />
                    <Button
                      variant="ghost"
                      size="sm"
                      icon={Trash2}
                      aria-label="Remove deduction"
                      onClick={() => setDeductions((d) => d.filter((_, j) => j !== i))}
                    />
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="mt-6 flex justify-end">
            <Button icon={Calculator} loading={submitting} onClick={handleCalculate}>
              Calculate
            </Button>
          </div>
        </Card>
      )}

      {/* Breakdown summary */}
      {breakdown && (
        <Card>
          <div className="flex items-center justify-between">
            <h2 className="font-heading text-base font-semibold text-foreground">
              Farmer settlement
            </h2>
            {isConfirmed && (
              <span className="inline-flex items-center gap-1.5 rounded-full bg-primary-soft px-2.5 py-1 text-xs font-medium text-primary">
                <ShieldCheck className="h-3.5 w-3.5" aria-hidden="true" /> Confirmed
              </span>
            )}
          </div>
          <dl className="mt-4 space-y-2 text-sm">
            <Line label="Gross amount" value={formatMoney(breakdown.grossAmount, breakdown.currency)} />
            <Line label="Commission" value={`− ${formatMoney(breakdown.commissionAmount, breakdown.currency)}`} />
            <Line label="Deductions" value={`− ${formatMoney(breakdown.deductionsTotal, breakdown.currency)}`} />
            <div className="flex items-center justify-between border-t border-border pt-2 text-base font-semibold text-foreground">
              <span>Farmer net amount</span>
              <span>{formatMoney(breakdown.netAmount, breakdown.currency)}</span>
            </div>
          </dl>

          {isConfirmed ? (
            <div className="mt-5 space-y-3">
              <p className="text-sm text-muted-foreground">
                Confirmed{existing?.confirmedBy ? ` by ${existing.confirmedBy}` : ''}
                {existing?.confirmedAt ? ` · ${formatDateTime(existing.confirmedAt)}` : ''}. This
                settlement is now part of the farmer's financial record and is read-only.
              </p>
              {balances && (
                <Alert variant="success" title="Farmer balance updated">
                  {balances.balances.length === 0
                    ? 'No outstanding balance.'
                    : balances.balances
                        .map((b) => formatMoney(b.outstanding, b.currency))
                        .join(' · ')}{' '}
                  outstanding.
                </Alert>
              )}
              <div className="flex gap-2">
                <Button variant="outline" onClick={() => navigate(`/farmers/${existing?.farmerId}`)}>
                  View farmer finances
                </Button>
                <Button variant="outline" onClick={() => navigate(`/deliveries/${deliveryId}`)}>
                  Back to delivery
                </Button>
              </div>
            </div>
          ) : (
            <div className="mt-5 flex justify-end">
              <Button
                icon={ShieldCheck}
                disabled={!canConfirm}
                onClick={() => setConfirmOpen(true)}
              >
                Confirm settlement
              </Button>
            </div>
          )}
          {!isConfirmed && !canConfirm && (
            <p className="mt-2 text-end text-xs text-muted-foreground">
              Your role cannot confirm settlements.
            </p>
          )}
        </Card>
      )}

      <Modal
        open={confirmOpen}
        onClose={() => (submitting ? undefined : setConfirmOpen(false))}
        title="Confirm settlement?"
        description="This freezes the calculation and credits the farmer's ledger. It cannot be edited afterwards."
        footer={
          <>
            <Button variant="outline" onClick={() => setConfirmOpen(false)} disabled={submitting}>
              Cancel
            </Button>
            <Button icon={ShieldCheck} loading={submitting} onClick={handleConfirm}>
              Confirm
            </Button>
          </>
        }
      >
        {breakdown && (
          <p className="text-sm text-foreground">
            Farmer net amount:{' '}
            <span className="font-semibold">{formatMoney(breakdown.netAmount, breakdown.currency)}</span>
          </p>
        )}
      </Modal>
    </div>
  )
}

function Fact({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-xs text-muted-foreground">{label}</dt>
      <dd className="font-medium text-foreground">{value}</dd>
    </div>
  )
}

function Line({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between text-muted-foreground">
      <span>{label}</span>
      <span className="font-medium text-foreground">{value}</span>
    </div>
  )
}
