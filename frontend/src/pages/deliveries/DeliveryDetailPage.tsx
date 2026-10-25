import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import {
  ArrowLeft,
  Scale,
  ClipboardCheck,
  Boxes,
  History,
  Truck,
  Wallet,
  Gavel,
} from 'lucide-react'
import {
  Card,
  CardHeader,
  Button,
  LoadingSpinner,
  EmptyState,
  Alert,
  IntakeStatusBadge,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/hooks/useAuth'
import { deliveryService } from '@/services/deliveryService'
import { productService } from '@/services/productService'
import { inventoryService } from '@/services/inventoryService'
import { gradeSuggestionService } from '@/services/gradeSuggestionService'
import { can } from '@/utils/permissions'
import { formatDate, formatDateTime, formatNumber } from '@/utils/format'
import { WeighDeliveryModal } from './WeighDeliveryModal'
import { GradeDeliveryModal } from './GradeDeliveryModal'
import { SuggestGradeModal } from './SuggestGradeModal'
import { DeliveryPhotos } from './DeliveryPhotos'

export function DeliveryDetailPage() {
  const { id = '' } = useParams()
  const deliveryId = Number(id)
  const navigate = useNavigate()
  const { user } = useAuth()
  const [reloadKey, setReloadKey] = useState(0)
  const [weighOpen, setWeighOpen] = useState(false)
  const [gradeOpen, setGradeOpen] = useState(false)
  const [suggestOpen, setSuggestOpen] = useState(false)

  // One bundle so the screen survives a refresh and optional sections (weight,
  // grade, inventory, audit) degrade to empty states instead of fake data.
  const { data, loading, error } = useAsync(async () => {
    const delivery = await deliveryService.getById(deliveryId)
    const [weight, grade, audit, products, suggestions] = await Promise.all([
      deliveryService.getWeight(deliveryId).catch(() => null),
      deliveryService.getGrade(deliveryId).catch(() => null),
      deliveryService.auditHistory(deliveryId).catch(() => []),
      productService.list({ size: 100 }).catch(() => null),
      gradeSuggestionService.listForDelivery(deliveryId).catch(() => []),
    ])
    const productId =
      products?.content.find((p) => p.name === delivery.productName)?.id ?? null
    const balance =
      productId != null
        ? await inventoryService.balanceForProduct(productId).catch(() => null)
        : null
    return { delivery, weight, grade, audit, balance, productId, suggestions }
  }, [deliveryId, reloadKey])

  const role = user?.role
  const canWeigh = role === 'receiving' || role === 'manager' || role === 'admin'
  const canGrade = role === 'inspector' || role === 'manager' || role === 'admin'
  const canSettle = role === 'accountant' || role === 'manager' || role === 'admin'
  const canUploadPhotos =
    role === 'receiving' || role === 'inspector' || role === 'manager' || role === 'admin'
  const reload = () => setReloadKey((k) => k + 1)

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner label="Loading delivery…" />
      </div>
    )
  }

  if (error || !data) {
    return (
      <EmptyState
        icon={Truck}
        title="Delivery not found"
        description={error ?? 'This delivery may have been removed or the link is incorrect.'}
        action={
          <Button variant="outline" icon={ArrowLeft} onClick={() => navigate('/deliveries')}>
            Back to Deliveries
          </Button>
        }
      />
    )
  }

  const { delivery, weight, grade, audit, balance, productId, suggestions } = data

  // A second-opinion suggestion only makes sense once the delivery has a grade,
  // and only one open suggestion is allowed at a time.
  const hasPendingSuggestion = suggestions.some((s) => s.status === 'PENDING')
  const canSuggest = can(role, 'grade.suggest') && grade != null && !hasPendingSuggestion

  const facts = [
    { label: 'Farmer', value: `${delivery.farmerName} (${delivery.farmerCode})` },
    { label: 'Product', value: delivery.productName },
    { label: 'Quantity', value: `${formatNumber(delivery.quantity)} ${delivery.unit}` },
    { label: 'Delivery date', value: formatDate(delivery.deliveryDate) },
    { label: 'Recorded by', value: delivery.createdBy ?? '—' },
    { label: 'Created', value: formatDateTime(delivery.createdAt) },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-1 text-sm text-muted-foreground">
        <Link to="/deliveries" className="hover:text-foreground hover:underline">
          Deliveries
        </Link>
        <span aria-hidden="true">/</span>
        <span className="text-foreground">#{delivery.id}</span>
      </div>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="font-heading text-xl font-semibold text-foreground sm:text-2xl">
              Delivery #{delivery.id}
            </h1>
            <IntakeStatusBadge status={delivery.status} />
          </div>
          <p className="mt-0.5 text-sm text-muted-foreground">
            {delivery.farmerName} · {delivery.productName}
          </p>
        </div>
        <div className="flex items-center gap-2">
          {delivery.status === 'PENDING' && canWeigh && (
            <Button icon={Scale} onClick={() => setWeighOpen(true)}>
              Record weight
            </Button>
          )}
          {delivery.status === 'WEIGHED' && canGrade && (
            <Button icon={ClipboardCheck} onClick={() => setGradeOpen(true)}>
              Grade
            </Button>
          )}
          {delivery.status === 'CONFIRMED' && canSettle && (
            <Button icon={Wallet} onClick={() => navigate(`/deliveries/${delivery.id}/settlement`)}>
              Settlement
            </Button>
          )}
          <Button variant="outline" icon={ArrowLeft} onClick={() => navigate('/deliveries')}>
            Back
          </Button>
        </div>
      </div>

      <Card>
        <dl className="grid grid-cols-1 gap-x-6 gap-y-5 sm:grid-cols-2 lg:grid-cols-3">
          {facts.map((f) => (
            <div key={f.label}>
              <dt className="text-xs text-muted-foreground">{f.label}</dt>
              <dd className="font-medium text-foreground">{f.value}</dd>
            </div>
          ))}
          {delivery.notes && (
            <div className="sm:col-span-2 lg:col-span-3">
              <dt className="text-xs text-muted-foreground">Notes</dt>
              <dd className="text-foreground">{delivery.notes}</dd>
            </div>
          )}
        </dl>
      </Card>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {/* Weight */}
        <Card flush>
          <CardHeader title="Weight" />
          <div className="p-5">
            {weight ? (
              <dl className="grid grid-cols-3 gap-4 text-center">
                <WeightCell label="Gross" value={`${formatNumber(weight.grossWeight)}`} />
                <WeightCell label="Tare" value={`${formatNumber(weight.tareWeight)}`} />
                <WeightCell
                  label="Net"
                  value={`${formatNumber(weight.netWeight)}`}
                  emphasize
                />
              </dl>
            ) : (
              <EmptyState
                icon={Scale}
                title="Not weighed yet"
                action={
                  delivery.status === 'PENDING' && canWeigh ? (
                    <Button size="sm" icon={Scale} onClick={() => setWeighOpen(true)}>
                      Record weight
                    </Button>
                  ) : undefined
                }
              />
            )}
          </div>
        </Card>

        {/* Grade */}
        <Card flush>
          <CardHeader title="Grade" />
          <div className="p-5">
            {grade ? (
              <div>
                <p className="text-lg font-semibold text-foreground">
                  {grade.gradeName}{' '}
                  <span className="font-mono text-sm text-muted-foreground">
                    {grade.gradeCode}
                  </span>
                </p>
                {grade.gradedBy && (
                  <p className="mt-1 text-sm text-muted-foreground">
                    Graded by {grade.gradedBy} · {formatDateTime(grade.gradedAt)}
                  </p>
                )}
                {grade.notes && (
                  <p className="mt-2 rounded-md bg-muted px-3 py-2 text-sm text-foreground">
                    <span className="font-medium">Inspection note:</span>{' '}
                    {grade.notes}
                  </p>
                )}
              </div>
            ) : (
              <EmptyState
                icon={ClipboardCheck}
                title="Not graded yet"
                action={
                  delivery.status === 'WEIGHED' && canGrade ? (
                    <Button
                      size="sm"
                      icon={ClipboardCheck}
                      onClick={() => setGradeOpen(true)}
                    >
                      Grade delivery
                    </Button>
                  ) : undefined
                }
              />
            )}
          </div>
        </Card>

        {/* Inventory */}
        <Card flush>
          <CardHeader
            title="Inventory"
            description="Current stock for this product"
          />
          <div className="p-5">
            {balance != null ? (
              <p className="text-2xl font-semibold text-foreground">
                {formatNumber(balance)}{' '}
                <span className="text-base font-normal text-muted-foreground">
                  {delivery.unit} in stock
                </span>
              </p>
            ) : (
              <EmptyState
                icon={Boxes}
                title="No stock recorded"
                description="A confirmed delivery adds incoming stock here."
              />
            )}
          </div>
        </Card>

        {/* Audit */}
        <Card flush>
          <CardHeader title="Audit history" />
          <div className="p-5">
            {audit.length > 0 ? (
              <ol className="space-y-4">
                {audit.map((entry) => (
                  <li key={entry.id} className="flex gap-3">
                    <span className="mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
                      <History className="h-3.5 w-3.5" aria-hidden="true" />
                    </span>
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-foreground">
                        {entry.action.replaceAll('_', ' ')}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {entry.previousStatus
                          ? `${entry.previousStatus} → ${entry.newStatus}`
                          : entry.newStatus}{' '}
                        · {entry.performedBy} · {formatDateTime(entry.createdAt)}
                      </p>
                      {entry.notes && (
                        <p className="mt-0.5 text-xs text-muted-foreground">{entry.notes}</p>
                      )}
                    </div>
                  </li>
                ))}
              </ol>
            ) : (
              <EmptyState icon={History} title="No audit records yet" />
            )}
          </div>
        </Card>
      </div>

      <DeliveryPhotos deliveryId={delivery.id} canUpload={canUploadPhotos} />

      {/* Grade review: second-opinion suggestions + their approval status. */}
      {(grade != null || suggestions.length > 0) && (
        <Card flush>
          <CardHeader
            title="Grade review"
            description="Second-opinion suggestions and manager decisions"
            action={
              canSuggest ? (
                <Button size="sm" variant="outline" icon={Gavel} onClick={() => setSuggestOpen(true)}>
                  Suggest different grade
                </Button>
              ) : undefined
            }
          />
          <div className="p-5">
            {suggestions.length === 0 ? (
              <EmptyState
                icon={Gavel}
                title="No suggestions"
                description={
                  canSuggest
                    ? 'Disagree with the grade? Propose a change for a manager to approve.'
                    : hasPendingSuggestion
                      ? 'A suggestion is awaiting manager review.'
                      : 'Inspector grade suggestions will appear here.'
                }
              />
            ) : (
              <ul className="space-y-4">
                {suggestions.map((s) => (
                  <li key={s.id} className="rounded-md border border-border p-3">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <p className="text-sm font-medium text-foreground">
                        {(s.currentGradeName ?? s.currentGradeCode ?? 'current')} →{' '}
                        <span className="text-foreground">{s.suggestedGradeName}</span>{' '}
                        <span className="font-mono text-xs text-muted-foreground">
                          {s.suggestedGradeCode}
                        </span>
                      </p>
                      <SuggestionStatusBadge status={s.status} />
                    </div>
                    <p className="mt-1.5 rounded-md bg-muted px-3 py-2 text-sm text-foreground">
                      {s.reason}
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      Suggested by {s.suggestedBy ?? 'inspector'} · {formatDateTime(s.createdAt)}
                    </p>
                    {s.status !== 'PENDING' && (
                      <p className="mt-0.5 text-xs text-muted-foreground">
                        {s.status === 'APPROVED' ? 'Approved' : 'Rejected'} by{' '}
                        {s.reviewedBy ?? 'manager'}
                        {s.reviewedAt ? ` · ${formatDateTime(s.reviewedAt)}` : ''}
                        {s.reviewNotes ? `: ${s.reviewNotes}` : ''}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </Card>
      )}

      {delivery.status === 'REJECTED' && (
        <Alert variant="warning" title="Rejected delivery">
          This delivery was graded as rejected and did not add inventory.
        </Alert>
      )}

      {weighOpen && (
        <WeighDeliveryModal
          deliveryId={delivery.id}
          onClose={() => setWeighOpen(false)}
          onDone={() => {
            setWeighOpen(false)
            reload()
          }}
        />
      )}

      {gradeOpen && (
        <GradeDeliveryModal
          deliveryId={delivery.id}
          productId={productId}
          onClose={() => setGradeOpen(false)}
          onDone={() => {
            setGradeOpen(false)
            reload()
          }}
        />
      )}

      {suggestOpen && (
        <SuggestGradeModal
          deliveryId={delivery.id}
          productId={productId}
          currentGradeCode={grade?.gradeCode ?? null}
          onClose={() => setSuggestOpen(false)}
          onDone={() => {
            setSuggestOpen(false)
            reload()
          }}
        />
      )}
    </div>
  )
}

function SuggestionStatusBadge({ status }: { status: 'PENDING' | 'APPROVED' | 'REJECTED' }) {
  const styles: Record<typeof status, string> = {
    PENDING: 'bg-warning-soft text-foreground',
    APPROVED: 'bg-success-soft text-foreground',
    REJECTED: 'bg-danger-soft text-foreground',
  }
  const label = status.charAt(0) + status.slice(1).toLowerCase()
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${styles[status]}`}>
      {label}
    </span>
  )
}

function WeightCell({
  label,
  value,
  emphasize,
}: {
  label: string
  value: string
  emphasize?: boolean
}) {
  return (
    <div>
      <dt className="text-xs text-muted-foreground">{label} (kg)</dt>
      <dd
        className={
          emphasize
            ? 'text-xl font-semibold text-foreground'
            : 'text-lg font-medium text-foreground'
        }
      >
        {value}
      </dd>
    </div>
  )
}
