import { useMemo, useState } from 'react'
import { Wallet, CircleDollarSign, Clock } from 'lucide-react'
import {
  PageHeader,
  Card,
  StatCard,
  SearchInput,
  Select,
  Table,
  Button,
  PaymentStatusBadge,
  EmptyState,
  Modal,
  Alert,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { paymentService } from '@/services/paymentService'
import { formatCurrency } from '@/utils/format'
import type { Payment } from '@/types'

export function PaymentsPage() {
  const { data, loading } = useAsync(() => paymentService.list())
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')
  const [selected, setSelected] = useState<Payment | null>(null)

  const payments = useMemo(() => data ?? [], [data])

  const totals = useMemo(() => {
    const owed = payments.reduce((s, p) => s + p.amountOwed, 0)
    const paid = payments.reduce((s, p) => s + p.amountPaid, 0)
    const remaining = payments.reduce((s, p) => s + p.remaining, 0)
    return { owed, paid, remaining }
  }, [payments])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    return payments.filter((p) => {
      const matchesQuery =
        !q ||
        p.farmerName.toLowerCase().includes(q) ||
        p.id.toLowerCase().includes(q)
      const matchesStatus = !status || p.status === status
      return matchesQuery && matchesStatus
    })
  }, [payments, query, status])

  const columns: Column<Payment>[] = [
    { header: 'Farmer', cell: (p) => <span className="font-medium">{p.farmerName}</span> },
    {
      header: 'Amount Owed',
      align: 'end',
      className: 'hidden sm:table-cell',
      cell: (p) => formatCurrency(p.amountOwed),
    },
    {
      header: 'Amount Paid',
      align: 'end',
      className: 'hidden md:table-cell',
      cell: (p) => formatCurrency(p.amountPaid),
    },
    {
      header: 'Remaining',
      align: 'end',
      cell: (p) => (
        <span className={p.remaining > 0 ? 'font-medium text-accent' : 'text-muted-foreground'}>
          {formatCurrency(p.remaining)}
        </span>
      ),
    },
    { header: 'Status', cell: (p) => <PaymentStatusBadge status={p.status} /> },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Payments"
        description="Track what the cooperative owes its farmers"
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard
          label="Total Owed"
          value={loading ? '—' : formatCurrency(totals.owed)}
          icon={Wallet}
          tone="neutral"
        />
        <StatCard
          label="Total Paid"
          value={loading ? '—' : formatCurrency(totals.paid)}
          icon={CircleDollarSign}
          tone="success"
        />
        <StatCard
          label="Outstanding"
          value={loading ? '—' : formatCurrency(totals.remaining)}
          icon={Clock}
          tone="accent"
          caption="pending payout"
        />
      </div>

      <Card flush>
        <div className="flex flex-col gap-3 border-b border-border p-4 sm:flex-row sm:items-center">
          <SearchInput
            value={query}
            onChange={setQuery}
            placeholder="Search by farmer or payment ID…"
            containerClassName="sm:max-w-xs sm:flex-1"
          />
          <Select
            aria-label="Filter by status"
            placeholder="All statuses"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            options={[
              { label: 'Paid', value: 'paid' },
              { label: 'Partially Paid', value: 'partial' },
              { label: 'Pending', value: 'pending' },
            ]}
            containerClassName="sm:w-48"
          />
        </div>

        <Table
          columns={columns}
          rows={filtered}
          rowKey={(p) => p.id}
          loading={loading}
          onRowClick={setSelected}
          empty={
            <EmptyState
              icon={Wallet}
              title="No payments found"
              description="Payments are created when deliveries are confirmed and priced."
            />
          }
        />
      </Card>

      <Modal
        open={selected !== null}
        onClose={() => setSelected(null)}
        title={selected ? `Payment · ${selected.farmerName}` : 'Payment'}
        description={selected?.id}
        footer={
          <>
            <Button variant="outline" onClick={() => setSelected(null)}>
              Close
            </Button>
            <Button icon={CircleDollarSign} onClick={() => setSelected(null)}>
              Record Payment
            </Button>
          </>
        }
      >
        {selected && (
          <div className="space-y-4">
            <dl className="grid grid-cols-3 gap-3 text-center">
              <div className="rounded-md bg-muted p-3">
                <dt className="text-xs text-muted-foreground">Owed</dt>
                <dd className="mt-1 font-semibold">{formatCurrency(selected.amountOwed)}</dd>
              </div>
              <div className="rounded-md bg-success-soft p-3">
                <dt className="text-xs text-muted-foreground">Paid</dt>
                <dd className="mt-1 font-semibold text-success">
                  {formatCurrency(selected.amountPaid)}
                </dd>
              </div>
              <div className="rounded-md bg-accent-soft p-3">
                <dt className="text-xs text-muted-foreground">Remaining</dt>
                <dd className="mt-1 font-semibold text-accent">
                  {formatCurrency(selected.remaining)}
                </dd>
              </div>
            </dl>
            <Alert variant="info">
              Recording payments is a later-week feature. This preview shows how
              a farmer's balance breakdown will be presented.
            </Alert>
          </div>
        )}
      </Modal>
    </div>
  )
}
