import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Check, ArrowLeft, ArrowRight, ShieldCheck, Scale } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  Input,
  Select,
  SearchInput,
  Table,
  Alert,
  EmptyState,
  LoadingSpinner,
  IntakeStatusBadge,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { useDebouncedValue } from '@/hooks/useDebouncedValue'
import { deliveryService } from '@/services/deliveryService'
import { farmerService } from '@/services/farmerService'
import { productService } from '@/services/productService'
import { gradeService } from '@/services/gradeService'
import { cn } from '@/utils/cn'
import { formatNumber } from '@/utils/format'
import { ApiError } from '@/services/api'
import type {
  DeliveryGradeResponse,
  DeliveryResponse,
  DeliveryWeightResponse,
  Farmer,
  GradeDefinition,
  Product,
} from '@/types'

const STEPS = ['Farmer', 'Product', 'Weight', 'Grade', 'Review'] as const
const today = () => new Date().toISOString().slice(0, 10)
const round3 = (n: number) => Math.round(n * 1000) / 1000

function errorMessage(err: unknown): string {
  if (err instanceof ApiError) return err.message
  if (err instanceof Error) return err.message
  return 'Something went wrong. Please try again.'
}

function Stepper({ step }: { step: number }) {
  return (
    <ol className="flex flex-wrap items-center gap-2 text-sm">
      {STEPS.map((label, i) => {
        const done = i < step
        const current = i === step
        return (
          <li key={label} className="flex items-center gap-2">
            <span
              className={cn(
                'flex h-7 w-7 items-center justify-center rounded-full text-xs font-semibold',
                done && 'bg-primary text-on-primary',
                current && 'bg-primary-soft text-primary ring-2 ring-primary',
                !done && !current && 'bg-muted text-muted-foreground',
              )}
            >
              {done ? <Check className="h-4 w-4" aria-hidden="true" /> : i + 1}
            </span>
            <span
              className={cn(
                'font-medium',
                current ? 'text-foreground' : 'text-muted-foreground',
              )}
            >
              {label}
            </span>
            {i < STEPS.length - 1 && (
              <ArrowRight className="h-3.5 w-3.5 text-muted-foreground/40" aria-hidden="true" />
            )}
          </li>
        )
      })}
    </ol>
  )
}

export function NewDeliveryWizard() {
  const navigate = useNavigate()
  const [step, setStep] = useState(0)

  // Selections
  const [farmer, setFarmer] = useState<Farmer | null>(null)
  const [product, setProduct] = useState<Product | null>(null)
  const [deliveryDate, setDeliveryDate] = useState(today())
  const [notes, setNotes] = useState('')
  const [gross, setGross] = useState('')
  const [tare, setTare] = useState('')
  const [gradeCode, setGradeCode] = useState('')
  const [gradeNotes, setGradeNotes] = useState('')

  // Backend results (source of truth once created)
  const [delivery, setDelivery] = useState<DeliveryResponse | null>(null)
  const [weight, setWeight] = useState<DeliveryWeightResponse | null>(null)
  const [grade, setGrade] = useState<DeliveryGradeResponse | null>(null)

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const grossNum = Number(gross)
  const tareNum = Number(tare)
  const netPreview =
    gross !== '' && tare !== '' && !Number.isNaN(grossNum) && !Number.isNaN(tareNum)
      ? round3(grossNum - tareNum)
      : null

  const deliveryId = delivery?.id ?? null

  // ---- Step 0: farmer search ------------------------------------------
  const [farmerQuery, setFarmerQuery] = useState('')
  const debouncedQuery = useDebouncedValue(farmerQuery, 300)
  const { data: farmersPage, loading: farmersLoading } = useAsync(
    () => farmerService.list({ search: debouncedQuery, size: 8 }),
    [debouncedQuery],
  )

  // ---- Step 1: products -----------------------------------------------
  const { data: productsPage, loading: productsLoading } = useAsync(
    () => productService.list({ size: 100 }),
    [],
  )
  const activeProducts = useMemo(
    () => (productsPage?.content ?? []).filter((p) => p.active),
    [productsPage],
  )

  // ---- Step 3: product grades -----------------------------------------
  const { data: grades, loading: gradesLoading } = useAsync<GradeDefinition[]>(
    () => (product ? gradeService.listByProduct(product.id) : Promise.resolve([])),
    [product?.id],
  )
  const activeGrades = useMemo(
    () => (grades ?? []).filter((g) => g.active),
    [grades],
  )

  function goBack() {
    setError(null)
    setStep((s) => Math.max(0, s - 1))
  }

  // Weight step commits the draft delivery (quantity must equal net weight on
  // the backend), then captures gross/tare so the server returns net weight.
  async function submitWeight() {
    setError(null)
    if (netPreview === null || grossNum <= 0) {
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
    if (weight) {
      setStep(3)
      return
    }
    if (!farmer || !product) {
      setError('Farmer and product must be selected first.')
      return
    }
    setSubmitting(true)
    try {
      const created =
        delivery ??
        (await deliveryService.create({
          farmerId: farmer.id,
          productId: product.id,
          quantity: netPreview,
          deliveryDate,
          notes: notes.trim() || undefined,
        }))
      setDelivery(created)
      const w = await deliveryService.captureWeight(created.id, {
        grossWeight: round3(grossNum),
        tareWeight: round3(tareNum),
      })
      setWeight(w)
      setStep(3)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function submitGrade() {
    setError(null)
    if (grade) {
      setStep(4)
      return
    }
    if (!gradeCode) {
      setError('Select a grade to continue.')
      return
    }
    if (!deliveryId) {
      setError('Delivery not created yet.')
      return
    }
    setSubmitting(true)
    try {
      const g = await deliveryService.captureGrade(deliveryId, {
        grade: gradeCode,
        notes: gradeNotes.trim() || undefined,
      })
      setGrade(g)
      // Grading advances the backend status — refresh our copy.
      setDelivery(await deliveryService.getById(deliveryId))
      setStep(4)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function confirmDelivery() {
    if (!deliveryId) return
    setError(null)
    setSubmitting(true)
    try {
      await deliveryService.confirm(deliveryId)
      navigate(`/deliveries/${deliveryId}`)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  const farmerColumns: Column<Farmer>[] = [
    { header: 'Code', cell: (f) => <span className="font-mono text-xs">{f.farmerCode}</span> },
    { header: 'Name', cell: (f) => <span className="font-medium">{f.name}</span> },
    { header: 'Village', className: 'hidden sm:table-cell', cell: (f) => f.village },
    { header: 'Phone', className: 'hidden md:table-cell', cell: (f) => f.phone },
  ]

  const locked = Boolean(delivery) // farmer/product committed after draft creation

  return (
    <div className="space-y-6">
      <PageHeader
        title="New Delivery"
        description="Guided intake: select the farmer and product, weigh, grade, then confirm."
        actions={
          <Button variant="outline" icon={ArrowLeft} onClick={() => navigate('/deliveries')}>
            Cancel
          </Button>
        }
      />

      <Card>
        <Stepper step={step} />
      </Card>

      {error && (
        <Alert variant="danger" title="Action failed" onDismiss={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* ---------- Step 0: Farmer ---------- */}
      {step === 0 && (
        <Card flush>
          <div className="border-b border-border p-4">
            <SearchInput
              value={farmerQuery}
              onChange={setFarmerQuery}
              placeholder="Search farmers by name, code, phone or village…"
            />
          </div>
          {farmer && (
            <div className="border-b border-border bg-primary-soft/40 px-4 py-3 text-sm">
              Selected farmer: <span className="font-semibold">{farmer.name}</span>{' '}
              <span className="font-mono text-xs text-muted-foreground">
                ({farmer.farmerCode})
              </span>
            </div>
          )}
          <Table
            columns={farmerColumns}
            rows={farmersPage?.content ?? []}
            rowKey={(f) => String(f.id)}
            loading={farmersLoading}
            onRowClick={(f) => setFarmer(f)}
            empty={
              <EmptyState
                title="No farmers found"
                description="Try a different search term."
              />
            }
          />
          <div className="flex justify-end border-t border-border p-4">
            <Button
              icon={ArrowRight}
              disabled={!farmer}
              onClick={() => setStep(1)}
            >
              Continue
            </Button>
          </div>
        </Card>
      )}

      {/* ---------- Step 1: Product ---------- */}
      {step === 1 && (
        <Card>
          <div className="space-y-4">
            {productsLoading ? (
              <div className="flex justify-center py-8">
                <LoadingSpinner label="Loading products…" />
              </div>
            ) : (
              <Select
                label="Product"
                required
                placeholder="Select a product"
                value={product ? String(product.id) : ''}
                disabled={locked}
                onChange={(e) => {
                  const p = activeProducts.find((x) => String(x.id) === e.target.value)
                  setProduct(p ?? null)
                  setGradeCode('')
                }}
                options={activeProducts.map((p) => ({
                  label: `${p.name}${p.variety ? ` — ${p.variety}` : ''} (${p.unit})`,
                  value: String(p.id),
                }))}
                hint={locked ? 'Locked — the draft delivery has been created.' : undefined}
              />
            )}
            <Input
              label="Delivery date"
              type="date"
              required
              value={deliveryDate}
              max={today()}
              disabled={locked}
              onChange={(e) => setDeliveryDate(e.target.value)}
            />
            <div className="flex flex-col gap-1.5">
              <label htmlFor="delivery-notes" className="text-sm font-medium text-foreground">
                Notes <span className="text-muted-foreground">(optional)</span>
              </label>
              <textarea
                id="delivery-notes"
                rows={3}
                value={notes}
                disabled={locked}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Anything worth recording about this delivery…"
                className="w-full rounded-md border border-input bg-card px-3.5 py-2.5 text-foreground placeholder:text-muted-foreground/70 transition-colors hover:border-muted-foreground/50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:opacity-60"
              />
            </div>
          </div>
          <div className="mt-6 flex justify-between">
            <Button variant="outline" icon={ArrowLeft} onClick={goBack}>
              Back
            </Button>
            <Button
              icon={ArrowRight}
              disabled={!product || !deliveryDate}
              onClick={() => setStep(2)}
            >
              Continue
            </Button>
          </div>
        </Card>
      )}

      {/* ---------- Step 2: Weight ---------- */}
      {step === 2 && (
        <Card>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Gross weight (kg)"
              type="number"
              required
              inputMode="decimal"
              min="0"
              step="0.001"
              value={gross}
              disabled={Boolean(weight) || submitting}
              onChange={(e) => setGross(e.target.value)}
            />
            <Input
              label="Tare weight (kg)"
              type="number"
              required
              inputMode="decimal"
              min="0"
              step="0.001"
              value={tare}
              disabled={Boolean(weight) || submitting}
              onChange={(e) => setTare(e.target.value)}
            />
          </div>

          <div className="mt-4 flex items-center gap-3 rounded-md bg-muted/60 px-4 py-3">
            <Scale className="h-5 w-5 text-muted-foreground" aria-hidden="true" />
            <div className="text-sm">
              <span className="text-muted-foreground">Net weight</span>
              <span className="ms-2 font-semibold text-foreground">
                {weight
                  ? `${formatNumber(weight.netWeight)} ${weight.unit}`
                  : netPreview !== null
                    ? `${formatNumber(netPreview)} kg (preview)`
                    : '—'}
              </span>
              {!weight && (
                <p className="mt-0.5 text-xs text-muted-foreground">
                  The confirmed net weight is calculated and stored by the backend.
                </p>
              )}
            </div>
          </div>

          <div className="mt-6 flex justify-between">
            <Button variant="outline" icon={ArrowLeft} onClick={goBack} disabled={submitting}>
              Back
            </Button>
            <Button
              icon={ArrowRight}
              loading={submitting}
              onClick={submitWeight}
            >
              {weight ? 'Continue' : 'Save weight & continue'}
            </Button>
          </div>
        </Card>
      )}

      {/* ---------- Step 3: Grade ---------- */}
      {step === 3 && (
        <Card>
          {gradesLoading ? (
            <div className="flex justify-center py-8">
              <LoadingSpinner label="Loading grades…" />
            </div>
          ) : activeGrades.length === 0 ? (
            <Alert variant="warning" title="No grades configured">
              This product has no active grade definitions. Add grades in Settings
              before grading deliveries.
            </Alert>
          ) : (
            <fieldset disabled={Boolean(grade) || submitting} className="space-y-2">
              <legend className="mb-2 text-sm font-medium text-foreground">
                Select a manual grade
              </legend>
              {activeGrades.map((g) => (
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
                    name="grade"
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

              <div className="pt-2">
                <label
                  htmlFor="grade-notes"
                  className="text-sm font-medium text-foreground"
                >
                  Inspection notes{' '}
                  <span className="font-normal text-muted-foreground">
                    (optional)
                  </span>
                </label>
                <textarea
                  id="grade-notes"
                  rows={2}
                  maxLength={500}
                  value={gradeNotes}
                  onChange={(e) => setGradeNotes(e.target.value)}
                  placeholder="e.g. minor bruising on ~10% of crates"
                  className="mt-1 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground/70 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
                />
              </div>
            </fieldset>
          )}

          <div className="mt-6 flex justify-between">
            <Button variant="outline" icon={ArrowLeft} onClick={goBack} disabled={submitting}>
              Back
            </Button>
            <Button
              icon={ArrowRight}
              loading={submitting}
              disabled={activeGrades.length === 0}
              onClick={submitGrade}
            >
              {grade ? 'Continue' : 'Save grade & continue'}
            </Button>
          </div>
        </Card>
      )}

      {/* ---------- Step 4: Review & confirm ---------- */}
      {step === 4 && (
        <Card>
          <h2 className="font-heading text-lg font-semibold text-foreground">
            Review delivery
          </h2>
          <p className="mt-0.5 text-sm text-muted-foreground">
            Check the details below, then confirm to commit the delivery and record
            incoming stock.
          </p>

          <dl className="mt-4 grid grid-cols-1 gap-x-6 gap-y-3 sm:grid-cols-2">
            <Fact label="Farmer" value={farmer ? `${farmer.name} (${farmer.farmerCode})` : '—'} />
            <Fact label="Product" value={product?.name ?? '—'} />
            <Fact label="Delivery date" value={deliveryDate} />
            <Fact
              label="Gross / Tare"
              value={weight ? `${formatNumber(weight.grossWeight)} / ${formatNumber(weight.tareWeight)} kg` : '—'}
            />
            <Fact
              label="Net weight"
              value={weight ? `${formatNumber(weight.netWeight)} ${weight.unit}` : '—'}
            />
            <Fact label="Grade" value={grade ? `${grade.gradeName} (${grade.gradeCode})` : '—'} />
            <Fact label="Notes" value={notes.trim() || '—'} />
            <div className="flex items-center gap-2">
              <dt className="text-xs text-muted-foreground">Status</dt>
              {delivery && <IntakeStatusBadge status={delivery.status} />}
            </div>
          </dl>

          <div className="mt-6 flex flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:justify-between">
            <Button variant="outline" icon={ArrowLeft} onClick={goBack} disabled={submitting}>
              Back
            </Button>
            {delivery?.status === 'REJECTED' ? (
              <div className="flex items-center gap-3">
                <Alert variant="warning">
                  This delivery was graded as rejected and will not be confirmed.
                </Alert>
                <Button onClick={() => navigate(`/deliveries/${deliveryId}`)}>
                  View delivery
                </Button>
              </div>
            ) : (
              <Button icon={ShieldCheck} loading={submitting} onClick={confirmDelivery}>
                Confirm delivery
              </Button>
            )}
          </div>
        </Card>
      )}
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
