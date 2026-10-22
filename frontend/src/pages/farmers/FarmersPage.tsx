import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { UserPlus, Users, Pencil, ChevronLeft, ChevronRight } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  SearchInput,
  Select,
  Table,
  FarmerStatusBadge,
  EmptyState,
  Alert,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { useDebouncedValue } from '@/hooks/useDebouncedValue'
import { farmerService } from '@/services/farmerService'
import { FarmerFormModal } from './FarmerFormModal'
import type { Farmer } from '@/types'

const PAGE_SIZE = 10

export function FarmersPage() {
  const navigate = useNavigate()

  const [query, setQuery] = useState('')
  const debouncedQuery = useDebouncedValue(query, 300)
  const [statusFilter, setStatusFilter] = useState('')
  const [page, setPage] = useState(0)
  const [refreshKey, setRefreshKey] = useState(0)

  const [addOpen, setAddOpen] = useState(false)
  const [editing, setEditing] = useState<Farmer | null>(null)

  // Reset to the first page whenever the search term changes.
  useEffect(() => {
    setPage(0)
  }, [debouncedQuery])

  const { data, loading, error } = useAsync(
    () => farmerService.list({ search: debouncedQuery, page, size: PAGE_SIZE }),
    [debouncedQuery, page, refreshKey],
  )

  const rows = useMemo(() => {
    const content = data?.content ?? []
    return statusFilter
      ? content.filter((f) => f.status === statusFilter)
      : content
  }, [data, statusFilter])

  const total = data?.totalElements ?? 0
  const totalPages = data?.totalPages ?? 1
  const isSearching = debouncedQuery.trim().length > 0

  const refresh = () => setRefreshKey((k) => k + 1)

  const columns: Column<Farmer>[] = [
    {
      header: 'Farmer Code',
      cell: (f) => (
        <span className="font-mono text-xs text-muted-foreground">
          {f.farmerCode}
        </span>
      ),
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
      header: 'Phone',
      className: 'hidden md:table-cell',
      cell: (f) => <span className="text-muted-foreground">{f.phone}</span>,
    },
    {
      header: 'Village',
      className: 'hidden sm:table-cell',
      cell: (f) => f.village,
    },
    { header: 'Status', cell: (f) => <FarmerStatusBadge status={f.status} /> },
    {
      header: 'Actions',
      align: 'end',
      srOnlyHeader: true,
      cell: (f) => (
        <Button
          variant="ghost"
          size="sm"
          icon={Pencil}
          aria-label={`Edit ${f.name}`}
          onClick={(e) => {
            e.stopPropagation()
            setEditing(f)
          }}
        >
          Edit
        </Button>
      ),
    },
  ]

  const rangeStart = total === 0 ? 0 : page * PAGE_SIZE + 1
  const rangeEnd = Math.min(total, page * PAGE_SIZE + (data?.content.length ?? 0))

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
            placeholder="Search by name, code, phone, or village…"
            containerClassName="sm:max-w-xs sm:flex-1"
          />
          <div className="flex gap-3">
            <Select
              aria-label="Filter by status"
              placeholder="All statuses"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              options={[
                { label: 'Active', value: 'ACTIVE' },
                { label: 'Inactive', value: 'INACTIVE' },
              ]}
              containerClassName="min-w-[10rem] flex-1"
            />
          </div>
        </div>

        {error ? (
          <div className="p-4">
            <Alert variant="danger" title="Couldn't load farmers">
              {error}
            </Alert>
          </div>
        ) : (
          <Table
            columns={columns}
            rows={rows}
            rowKey={(f) => String(f.id)}
            loading={loading}
            onRowClick={(f) => navigate(`/farmers/${f.id}`)}
            empty={
              isSearching || statusFilter ? (
                <EmptyState
                  icon={Users}
                  title="No matching farmers"
                  description="Try a different search term or clear the filters."
                />
              ) : (
                <EmptyState
                  icon={Users}
                  title="No farmers yet"
                  description="Add your first farmer to get started."
                  action={
                    <Button icon={UserPlus} onClick={() => setAddOpen(true)}>
                      Add Farmer
                    </Button>
                  }
                />
              )
            }
          />
        )}

        {!error && !loading && total > 0 && (
          <div className="flex flex-col gap-3 border-t border-border px-4 py-3 text-sm text-muted-foreground sm:flex-row sm:items-center sm:justify-between">
            <span>
              Showing {rangeStart}–{rangeEnd} of {total} farmer
              {total === 1 ? '' : 's'}
            </span>
            {totalPages > 1 && (
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  icon={ChevronLeft}
                  disabled={page <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                >
                  Previous
                </Button>
                <span className="px-1">
                  Page {page + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  icon={ChevronRight}
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                >
                  Next
                </Button>
              </div>
            )}
          </div>
        )}
      </Card>

      <FarmerFormModal
        open={addOpen}
        onClose={() => setAddOpen(false)}
        onSaved={() => {
          setPage(0)
          refresh()
        }}
      />
      <FarmerFormModal
        open={editing !== null}
        farmer={editing}
        onClose={() => setEditing(null)}
        onSaved={refresh}
      />
    </div>
  )
}
