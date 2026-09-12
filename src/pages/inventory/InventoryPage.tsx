import { useMemo, useState } from 'react'
import { Boxes, PackageMinus, Layers } from 'lucide-react'
import {
  PageHeader,
  Card,
  StatCard,
  SearchInput,
  Select,
  Table,
  InventoryStatusBadge,
  GradeBadge,
  EmptyState,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { inventoryService } from '@/services/inventoryService'
import { formatNumber, formatWeight, timeAgo } from '@/utils/format'
import type { InventoryItem } from '@/types'

export function InventoryPage() {
  const { data, loading } = useAsync(() => inventoryService.list())
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')

  const items = useMemo(() => data ?? [], [data])

  const totals = useMemo(() => {
    const totalKg = items.reduce((s, i) => s + i.quantity, 0)
    const inStock = items.filter((i) => i.quantity > 0).length
    const low = items.filter(
      (i) => i.status === 'low_stock' || i.status === 'out_of_stock',
    ).length
    return { totalKg, inStock, low }
  }, [items])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    return items.filter((i) => {
      const matchesQuery = !q || i.product.toLowerCase().includes(q)
      const matchesStatus = !status || i.status === status
      return matchesQuery && matchesStatus
    })
  }, [items, query, status])

  const columns: Column<InventoryItem>[] = [
    { header: 'Product', cell: (i) => <span className="font-medium">{i.product}</span> },
    { header: 'Grade', align: 'center', cell: (i) => <GradeBadge grade={i.grade} /> },
    {
      header: 'Available Quantity',
      align: 'end',
      cell: (i) => <span className="font-medium">{formatWeight(i.quantity)}</span>,
    },
    {
      header: 'Last Updated',
      className: 'hidden sm:table-cell',
      cell: (i) => <span className="text-muted-foreground">{timeAgo(i.lastUpdated)}</span>,
    },
    { header: 'Status', cell: (i) => <InventoryStatusBadge status={i.status} /> },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Inventory"
        description="Produce currently held at the collection center"
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard
          label="Total Inventory"
          value={loading ? '—' : formatWeight(totals.totalKg)}
          icon={Boxes}
          tone="primary"
        />
        <StatCard
          label="Products in Stock"
          value={loading ? '—' : formatNumber(totals.inStock)}
          icon={Layers}
          tone="info"
          caption="line items"
        />
        <StatCard
          label="Low Stock Items"
          value={loading ? '—' : formatNumber(totals.low)}
          icon={PackageMinus}
          tone="accent"
          caption="need attention"
        />
      </div>

      <Card flush>
        <div className="flex flex-col gap-3 border-b border-border p-4 sm:flex-row sm:items-center">
          <SearchInput
            value={query}
            onChange={setQuery}
            placeholder="Search products…"
            containerClassName="sm:max-w-xs sm:flex-1"
          />
          <Select
            aria-label="Filter by status"
            placeholder="All statuses"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            options={[
              { label: 'In Stock', value: 'in_stock' },
              { label: 'Low Stock', value: 'low_stock' },
              { label: 'Out of Stock', value: 'out_of_stock' },
            ]}
            containerClassName="sm:w-48"
          />
        </div>

        <Table
          columns={columns}
          rows={filtered}
          rowKey={(i) => i.id}
          loading={loading}
          empty={
            <EmptyState
              icon={Boxes}
              title="No inventory items"
              description="Confirmed deliveries will add produce to inventory here."
            />
          }
        />
      </Card>
    </div>
  )
}
