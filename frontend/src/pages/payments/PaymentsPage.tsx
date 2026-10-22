import { useMemo, useState } from 'react'
import { Wallet, CircleDollarSign } from 'lucide-react'
import {
  PageHeader,
  Card,
  SearchInput,
  Select,
  Table,
  Button,
  EmptyState,
  Alert,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/hooks/useAuth'
import { paymentService } from '@/services/paymentService'
import { formatMoney } from '@/utils/format'
import type { Currency, OutstandingFarmer } from '@/types'
import { RecordPaymentModal } from './RecordPaymentModal'

function balanceFor(farmer: OutstandingFarmer, currency: Currency): number {
  return farmer.balances.find((b) => b.currency === currency)?.outstanding ?? 0
}

export function PaymentsPage() {
  const { user } = useAuth()
  const canRecord = ['admin', 'manager', 'accountant'].includes(user?.role ?? '')

  const [refreshKey, setRefreshKey] = useState(0)
  const { data, loading, error } = useAsync(
    () => paymentService.getOutstandingFarmers(),
    [refreshKey],
  )
  const [query, setQuery] = useState('')
  const [currencyFilter, setCurrencyFilter] = useState('')
  const [selected, setSelected] = useState<OutstandingFarmer | null>(null)

  const farmers = useMemo(() => data ?? [], [data])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    return farmers.filter((f) => {
      const matchesQuery =
        !q ||
        (f.farmerName ?? '').toLowerCase().includes(q) ||
        (f.farmerCode ?? '').toLowerCase().includes(q)
      const matchesCurrency =
        !currencyFilter || balanceFor(f, currencyFilter as Currency) > 0
      return matchesQuery && matchesCurrency
    })
  }, [farmers, query, currencyFilter])

  const columns: Column<OutstandingFarmer>[] = [
    {
      header: 'Farmer',
      cell: (f) => (
        <div>
          <span className="font-medium">{f.farmerName ?? '—'}</span>
          {f.farmerCode && (
            <span className="ms-2 font-mono text-xs text-muted-foreground">{f.farmerCode}</span>
          )}
        </div>
      ),
    },
    {
      header: 'USD outstanding',
      align: 'end',
      cell: (f) => {
        const v = balanceFor(f, 'USD')
        return v > 0 ? formatMoney(v, 'USD') : <span className="text-muted-foreground">—</span>
      },
    },
    {
      header: 'LBP outstanding',
      align: 'end',
      cell: (f) => {
        const v = balanceFor(f, 'LBP')
        return v > 0 ? formatMoney(v, 'LBP') : <span className="text-muted-foreground">—</span>
      },
    },
    {
      header: '',
      align: 'end',
      srOnlyHeader: true,
      cell: (f) => (
        <Button
          size="sm"
          icon={CircleDollarSign}
          disabled={!canRecord}
          onClick={() => setSelected(f)}
        >
          Record
        </Button>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Payments"
        description="Farmers awaiting payment. USD and LBP balances are tracked separately."
      />

      {error && (
        <Alert variant="danger" title="Couldn't load outstanding balances">
          {error}
        </Alert>
      )}
      {!canRecord && (
        <Alert variant="info">
          Your role can view balances but cannot record payments.
        </Alert>
      )}

      <Card flush>
        <div className="flex flex-col gap-3 border-b border-border p-4 sm:flex-row sm:items-center">
          <SearchInput
            value={query}
            onChange={setQuery}
            placeholder="Search by farmer name or code…"
            containerClassName="sm:max-w-xs sm:flex-1"
          />
          <Select
            aria-label="Filter by currency"
            placeholder="All currencies"
            value={currencyFilter}
            onChange={(e) => setCurrencyFilter(e.target.value)}
            options={[
              { label: 'USD', value: 'USD' },
              { label: 'LBP', value: 'LBP' },
            ]}
            containerClassName="sm:w-48"
          />
        </div>

        <Table
          columns={columns}
          rows={filtered}
          rowKey={(f) => String(f.farmerId)}
          loading={loading}
          empty={
            <EmptyState
              icon={Wallet}
              title="No outstanding balances"
              description="Confirmed settlements that owe farmers money appear here."
            />
          }
        />
      </Card>

      {selected && (
        <RecordPaymentModal
          open={selected !== null}
          farmerId={selected.farmerId}
          farmerName={selected.farmerName ?? `Farmer #${selected.farmerId}`}
          balances={selected.balances}
          onClose={() => setSelected(null)}
          onRecorded={() => {
            setSelected(null)
            setRefreshKey((k) => k + 1)
          }}
        />
      )}
    </div>
  )
}
