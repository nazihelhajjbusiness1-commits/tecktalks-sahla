import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { UserPlus, Users } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  SearchInput,
  Select,
  Table,
  FarmerStatusBadge,
  EmptyState,
  Modal,
  Alert,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { farmerService } from '@/services/farmerService'
import { lebaneseRegions } from '@/services/mockData'
import { formatCurrency } from '@/utils/format'
import type { Farmer } from '@/types'

export function FarmersPage() {
  const navigate = useNavigate()
  const { data, loading } = useAsync(() => farmerService.list())
  const [query, setQuery] = useState('')
  const [region, setRegion] = useState('')
  const [status, setStatus] = useState('')
  const [addOpen, setAddOpen] = useState(false)

  const farmers = useMemo(() => data ?? [], [data])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    return farmers.filter((f) => {
      const matchesQuery =
        !q ||
        f.name.toLowerCase().includes(q) ||
        f.id.toLowerCase().includes(q) ||
        f.village.toLowerCase().includes(q)
      const matchesRegion = !region || f.region === region
      const matchesStatus = !status || f.status === status
      return matchesQuery && matchesRegion && matchesStatus
    })
  }, [farmers, query, region, status])

  const columns: Column<Farmer>[] = [
    {
      header: 'Farmer ID',
      cell: (f) => <span className="font-mono text-xs text-muted-foreground">{f.id}</span>,
    },
    {
      header: 'Name',
      cell: (f) => (
        <div>
          <p className="font-medium text-foreground">{f.name}</p>
          <p className="text-xs text-muted-foreground sm:hidden">{f.village}</p>
        </div>
      ),
    },
    {
      header: 'Village',
      className: 'hidden sm:table-cell',
      cell: (f) => (
        <span>
          {f.village}
          <span className="ms-1 text-xs text-muted-foreground">· {f.region}</span>
        </span>
      ),
    },
    { header: 'Main Crop', className: 'hidden md:table-cell', cell: (f) => f.mainCrop },
    {
      header: 'Deliveries',
      align: 'end',
      className: 'hidden lg:table-cell',
      cell: (f) => f.totalDeliveries,
    },
    {
      header: 'Balance',
      align: 'end',
      cell: (f) => (
        <span className={f.balance > 0 ? 'font-medium text-accent' : 'text-muted-foreground'}>
          {formatCurrency(f.balance)}
        </span>
      ),
    },
    { header: 'Status', cell: (f) => <FarmerStatusBadge status={f.status} /> },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Farmers"
        description="Manage the cooperative's registered farmers"
        actions={
          <Button icon={UserPlus} onClick={() => setAddOpen(true)}>
            Add Farmer
          </Button>
        }
      />

      <Card flush>
        <div className="flex flex-col gap-3 border-b border-border p-4 sm:flex-row sm:items-center">
          <SearchInput
            value={query}
            onChange={setQuery}
            placeholder="Search by name, ID, or village…"
            containerClassName="sm:max-w-xs sm:flex-1"
          />
          <div className="flex gap-3">
            <Select
              aria-label="Filter by region"
              placeholder="All regions"
              value={region}
              onChange={(e) => setRegion(e.target.value)}
              options={lebaneseRegions.map((r) => ({ label: r, value: r }))}
              containerClassName="min-w-[9rem] flex-1"
            />
            <Select
              aria-label="Filter by status"
              placeholder="All statuses"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              options={[
                { label: 'Active', value: 'active' },
                { label: 'Inactive', value: 'inactive' },
              ]}
              containerClassName="min-w-[9rem] flex-1"
            />
          </div>
        </div>

        <Table
          columns={columns}
          rows={filtered}
          rowKey={(f) => f.id}
          loading={loading}
          onRowClick={(f) => navigate(`/farmers/${f.id}`)}
          empty={
            <EmptyState
              icon={Users}
              title="No farmers found"
              description="Try adjusting your search or filters, or add a new farmer."
            />
          }
        />
        {!loading && filtered.length > 0 && (
          <div className="border-t border-border px-4 py-3 text-sm text-muted-foreground">
            Showing {filtered.length} of {farmers.length} farmers
          </div>
        )}
      </Card>

      <Modal
        open={addOpen}
        onClose={() => setAddOpen(false)}
        title="Add Farmer"
        description="Register a new farmer with the cooperative."
        footer={
          <>
            <Button variant="outline" onClick={() => setAddOpen(false)}>
              Cancel
            </Button>
            <Button onClick={() => setAddOpen(false)}>Save Farmer</Button>
          </>
        }
      >
        <Alert variant="info">
          The full farmer registration form arrives in a later week. This
          dialog demonstrates the modal pattern and where the form will live.
        </Alert>
      </Modal>
    </div>
  )
}
