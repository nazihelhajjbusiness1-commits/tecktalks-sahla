import { useMemo } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Plus, Truck, ChevronLeft, ChevronRight } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  Select,
  Table,
  IntakeStatusBadge,
  EmptyState,
  Alert,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { deliveryService } from '@/services/deliveryService'
import { farmerService } from '@/services/farmerService'
import { productService } from '@/services/productService'
import { formatDate, formatNumber } from '@/utils/format'
import type { DeliveryResponse, IntakeStatus } from '@/types'

const statusOptions: { label: string; value: IntakeStatus }[] = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'Weighed', value: 'WEIGHED' },
  { label: 'Graded', value: 'GRADED' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'Delivered', value: 'DELIVERED' },
  { label: 'Rejected', value: 'REJECTED' },
  { label: 'Cancelled', value: 'CANCELLED' },
]

export function DeliveriesPage() {
  const navigate = useNavigate()
  // Filters live in the URL so refreshing / sharing a link keeps the view.
  const [searchParams, setSearchParams] = useSearchParams()

  const farmerId = searchParams.get('farmerId') ?? ''
  const productId = searchParams.get('productId') ?? ''
  const status = searchParams.get('status') ?? ''
  const from = searchParams.get('from') ?? ''
  const to = searchParams.get('to') ?? ''
  const page = Number(searchParams.get('page') ?? '0')

  function setParam(key: string, value: string) {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev)
      if (value) next.set(key, value)
      else next.delete(key)
      // Any filter change resets to the first page.
      if (key !== 'page') next.delete('page')
      return next
    })
  }

  const { data: farmersPage } = useAsync(() => farmerService.list({ size: 100 }), [])
  const { data: productsPage } = useAsync(() => productService.list({ size: 100 }), [])

  const { data, loading, error } = useAsync(
    () =>
      deliveryService.list({
        farmerId: farmerId ? Number(farmerId) : undefined,
        productId: productId ? Number(productId) : undefined,
        status: (status || undefined) as IntakeStatus | undefined,
        from: from || undefined,
        to: to || undefined,
        page,
        size: 20,
      }),
    [farmerId, productId, status, from, to, page],
  )

  const deliveries = useMemo(() => data?.content ?? [], [data])

  const columns: Column<DeliveryResponse>[] = [
    {
      header: 'Delivery',
      cell: (d) => <span className="font-mono text-xs text-muted-foreground">#{d.id}</span>,
    },
    { header: 'Farmer', cell: (d) => <span className="font-medium">{d.farmerName}</span> },
    { header: 'Product', className: 'hidden sm:table-cell', cell: (d) => d.productName },
    {
      header: 'Quantity',
      align: 'end',
      cell: (d) => `${formatNumber(d.quantity)} ${d.unit}`,
    },
    {
      header: 'Date',
      className: 'hidden md:table-cell',
      cell: (d) => formatDate(d.deliveryDate),
    },
    { header: 'Status', cell: (d) => <IntakeStatusBadge status={d.status} /> },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Deliveries"
        description="Track produce from intake through grading and confirmation"
        actions={
          <Button icon={Plus} onClick={() => navigate('/deliveries/new')}>
            New Delivery
          </Button>
        }
      />

      {error && (
        <Alert variant="danger" title="Couldn't load deliveries">
          {error}
        </Alert>
      )}

      <Card flush>
        <div className="grid grid-cols-1 gap-3 border-b border-border p-4 sm:grid-cols-2 lg:grid-cols-4">
          <Select
            aria-label="Filter by farmer"
            placeholder="All farmers"
            value={farmerId}
            onChange={(e) => setParam('farmerId', e.target.value)}
            options={(farmersPage?.content ?? []).map((f) => ({
              label: `${f.name} (${f.farmerCode})`,
              value: String(f.id),
            }))}
          />
          <Select
            aria-label="Filter by product"
            placeholder="All products"
            value={productId}
            onChange={(e) => setParam('productId', e.target.value)}
            options={(productsPage?.content ?? []).map((p) => ({
              label: p.name,
              value: String(p.id),
            }))}
          />
          <Select
            aria-label="Filter by status"
            placeholder="All statuses"
            value={status}
            onChange={(e) => setParam('status', e.target.value)}
            options={statusOptions}
          />
          <div className="grid grid-cols-2 gap-2">
            <input
              type="date"
              aria-label="From date"
              value={from}
              max={to || undefined}
              onChange={(e) => setParam('from', e.target.value)}
              className="h-11 w-full rounded-md border border-input bg-card px-3 text-sm text-foreground transition-colors hover:border-muted-foreground/50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            />
            <input
              type="date"
              aria-label="To date"
              value={to}
              min={from || undefined}
              onChange={(e) => setParam('to', e.target.value)}
              className="h-11 w-full rounded-md border border-input bg-card px-3 text-sm text-foreground transition-colors hover:border-muted-foreground/50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            />
          </div>
        </div>

        <Table
          columns={columns}
          rows={deliveries}
          rowKey={(d) => String(d.id)}
          loading={loading}
          onRowClick={(d) => navigate(`/deliveries/${d.id}`)}
          empty={
            <EmptyState
              icon={Truck}
              title="No deliveries found"
              description="Adjust your filters or record a new delivery to get started."
            />
          }
        />

        {!loading && data && data.totalElements > 0 && (
          <div className="flex items-center justify-between gap-3 border-t border-border px-4 py-3 text-sm text-muted-foreground">
            <span>
              Page {data.number + 1} of {data.totalPages} · {data.totalElements}{' '}
              {data.totalElements === 1 ? 'delivery' : 'deliveries'}
            </span>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                icon={ChevronLeft}
                disabled={data.number <= 0}
                onClick={() => setParam('page', String(data.number - 1))}
              >
                Prev
              </Button>
              <Button
                variant="outline"
                size="sm"
                icon={ChevronRight}
                disabled={data.number + 1 >= data.totalPages}
                onClick={() => setParam('page', String(data.number + 1))}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  )
}
