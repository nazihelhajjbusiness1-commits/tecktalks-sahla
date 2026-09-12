import { useMemo, useState } from 'react'
import { Plus, Truck, ArrowRight } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  SearchInput,
  Select,
  Table,
  DeliveryStatusBadge,
  GradeBadge,
  EmptyState,
  Modal,
  Alert,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { deliveryService } from '@/services/deliveryService'
import { products } from '@/services/mockData'
import { formatDate, formatWeight } from '@/utils/format'
import type { Delivery, DeliveryStatus } from '@/types'

const statusOptions: { label: string; value: DeliveryStatus }[] = [
  { label: 'Draft', value: 'draft' },
  { label: 'Weighed', value: 'weighed' },
  { label: 'Grading', value: 'grading' },
  { label: 'Confirmed', value: 'confirmed' },
  { label: 'Completed', value: 'completed' },
  { label: 'Rejected', value: 'rejected' },
]

export function DeliveriesPage() {
  const { data, loading } = useAsync(() => deliveryService.list())
  const [query, setQuery] = useState('')
  const [product, setProduct] = useState('')
  const [status, setStatus] = useState('')
  const [date, setDate] = useState('')
  const [wizardOpen, setWizardOpen] = useState(false)

  const deliveries = useMemo(() => data ?? [], [data])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    return deliveries.filter((d) => {
      const matchesQuery =
        !q ||
        d.id.toLowerCase().includes(q) ||
        d.farmerName.toLowerCase().includes(q)
      const matchesProduct = !product || d.product === product
      const matchesStatus = !status || d.status === status
      const matchesDate = !date || d.date === date
      return matchesQuery && matchesProduct && matchesStatus && matchesDate
    })
  }, [deliveries, query, product, status, date])

  const columns: Column<Delivery>[] = [
    {
      header: 'Delivery ID',
      cell: (d) => <span className="font-mono text-xs text-muted-foreground">{d.id}</span>,
    },
    { header: 'Farmer', cell: (d) => <span className="font-medium">{d.farmerName}</span> },
    { header: 'Product', className: 'hidden sm:table-cell', cell: (d) => d.product },
    { header: 'Net Weight', align: 'end', cell: (d) => formatWeight(d.netWeight) },
    { header: 'Grade', align: 'center', cell: (d) => <GradeBadge grade={d.grade} /> },
    { header: 'Date', className: 'hidden md:table-cell', cell: (d) => formatDate(d.date) },
    { header: 'Status', cell: (d) => <DeliveryStatusBadge status={d.status} /> },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Deliveries"
        description="Track produce from intake through grading and confirmation"
        actions={
          <Button icon={Plus} onClick={() => setWizardOpen(true)}>
            New Delivery
          </Button>
        }
      />

      <Card flush>
        <div className="grid grid-cols-1 gap-3 border-b border-border p-4 sm:grid-cols-2 lg:grid-cols-4">
          <SearchInput
            value={query}
            onChange={setQuery}
            placeholder="Search ID or farmer…"
          />
          <Select
            aria-label="Filter by product"
            placeholder="All products"
            value={product}
            onChange={(e) => setProduct(e.target.value)}
            options={products.map((p) => ({ label: p.name, value: p.name }))}
          />
          <Select
            aria-label="Filter by status"
            placeholder="All statuses"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            options={statusOptions}
          />
          <input
            type="date"
            aria-label="Filter by date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="h-11 w-full rounded-md border border-input bg-card px-3.5 text-foreground transition-colors hover:border-muted-foreground/50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          />
        </div>

        <Table
          columns={columns}
          rows={filtered}
          rowKey={(d) => d.id}
          loading={loading}
          empty={
            <EmptyState
              icon={Truck}
              title="No deliveries found"
              description="Adjust your filters or record a new delivery to get started."
            />
          }
        />
        {!loading && filtered.length > 0 && (
          <div className="border-t border-border px-4 py-3 text-sm text-muted-foreground">
            Showing {filtered.length} of {deliveries.length} deliveries
          </div>
        )}
      </Card>

      <Modal
        open={wizardOpen}
        onClose={() => setWizardOpen(false)}
        title="New Delivery"
        description="Record produce arriving at the collection center."
        footer={
          <Button variant="outline" onClick={() => setWizardOpen(false)}>
            Close
          </Button>
        }
      >
        <Alert variant="info" title="Delivery wizard coming soon">
          The guided flow — select farmer → enter weight → grade → confirm →
          calculate payment → update inventory — is planned for a later week.
          The architecture already supports adding it here.
        </Alert>
        <ol className="mt-4 space-y-2 text-sm">
          {[
            'Select farmer',
            'Enter net weight',
            'Grade produce',
            'Confirm & price',
            'Update inventory',
          ].map((step, i) => (
            <li key={step} className="flex items-center gap-3 text-muted-foreground">
              <span className="flex h-6 w-6 items-center justify-center rounded-full bg-muted text-xs font-semibold text-foreground">
                {i + 1}
              </span>
              {step}
              {i < 4 && <ArrowRight className="ms-auto h-3.5 w-3.5 opacity-40" aria-hidden="true" />}
            </li>
          ))}
        </ol>
      </Modal>
    </div>
  )
}
