import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import {
  ArrowLeft,
  Phone,
  MapPin,
  Hash,
  CalendarDays,
  Pencil,
  Wallet,
} from 'lucide-react'
import {
  Card,
  CardHeader,
  Button,
  LoadingSpinner,
  EmptyState,
  FarmerStatusBadge,
  Select,
  Table,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { farmerService } from '@/services/farmerService'
import { paymentService } from '@/services/paymentService'
import { formatDate, formatDateTime, formatMoney, initials } from '@/utils/format'
import type { Currency, FarmerPayment, StatementLine } from '@/types'
import { FarmerFormModal } from './FarmerFormModal'

export function FarmerDetailPage() {
  const { id = '' } = useParams()
  const farmerId = Number(id)
  const navigate = useNavigate()
  const [editOpen, setEditOpen] = useState(false)
  const [refreshKey, setRefreshKey] = useState(0)

  const [currency, setCurrency] = useState('')

  const { data: farmer, loading } = useAsync(
    () => farmerService.getById(farmerId),
    [farmerId, refreshKey],
  )

  // Financial data (DT-77) — balances, statement, payment history from the ledger.
  const { data: finance, loading: financeLoading } = useAsync(async () => {
    const [balances, statement, payments] = await Promise.all([
      paymentService.getFarmerBalances(farmerId).catch(() => null),
      paymentService
        .getFarmerStatement(farmerId, { currency: (currency || undefined) as Currency | undefined })
        .catch(() => [] as StatementLine[]),
      paymentService
        .getFarmerPayments(farmerId, {
          currency: (currency || undefined) as Currency | undefined,
          size: 50,
        })
        .catch(() => null),
    ])
    return { balances, statement, payments }
  }, [farmerId, refreshKey, currency])

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner label="Loading farmer…" />
      </div>
    )
  }

  if (!farmer) {
    return (
      <EmptyState
        title="Farmer not found"
        description="This farmer may have been removed or the link is incorrect."
        action={
          <Button
            variant="outline"
            icon={ArrowLeft}
            onClick={() => navigate('/farmers')}
          >
            Back to Farmers
          </Button>
        }
      />
    )
  }

  const facts = [
    { icon: Hash, label: 'Farmer code', value: farmer.farmerCode },
    { icon: Phone, label: 'Phone', value: farmer.phone },
    { icon: MapPin, label: 'Village', value: farmer.village },
    { icon: CalendarDays, label: 'Registered', value: formatDate(farmer.createdAt) },
  ]

  const statementColumns: Column<StatementLine>[] = [
    { header: 'Date', cell: (l) => formatDate(l.occurredAt) },
    { header: 'Type', cell: (l) => l.entryType.replaceAll('_', ' ') },
    { header: 'Description', className: 'hidden sm:table-cell', cell: (l) => l.description ?? '—' },
    {
      header: 'Debit',
      align: 'end',
      cell: (l) => (l.debit != null ? formatMoney(l.debit, l.currency) : '—'),
    },
    {
      header: 'Credit',
      align: 'end',
      cell: (l) => (l.credit != null ? formatMoney(l.credit, l.currency) : '—'),
    },
    {
      header: 'Balance',
      align: 'end',
      cell: (l) => (
        <span className="font-medium">{formatMoney(l.runningBalance, l.currency)}</span>
      ),
    },
  ]

  const paymentColumns: Column<FarmerPayment>[] = [
    { header: 'Date', cell: (p) => formatDateTime(p.paidAt) },
    { header: 'Amount', align: 'end', cell: (p) => formatMoney(p.amount, p.currency) },
    { header: 'Method', cell: (p) => p.paymentMethod.replaceAll('_', ' ') },
    { header: 'Reference', className: 'hidden sm:table-cell', cell: (p) => p.referenceNumber ?? '—' },
    { header: 'Recorded by', className: 'hidden md:table-cell', cell: (p) => p.recordedBy ?? '—' },
  ]

  const balances = finance?.balances?.balances ?? []
  const statement = finance?.statement ?? []
  const payments = finance?.payments?.content ?? []

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-1 text-sm text-muted-foreground">
        <Link to="/farmers" className="hover:text-foreground hover:underline">
          Farmers
        </Link>
        <span aria-hidden="true">/</span>
        <span className="text-foreground">{farmer.name}</span>
      </div>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-center gap-4">
          <span className="flex h-14 w-14 items-center justify-center rounded-full bg-primary-soft text-lg font-semibold text-primary">
            {initials(farmer.name)}
          </span>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="font-heading text-xl font-semibold text-foreground sm:text-2xl">
                {farmer.name}
              </h1>
              <FarmerStatusBadge status={farmer.status} />
            </div>
            <p className="mt-0.5 font-mono text-xs text-muted-foreground">
              {farmer.farmerCode} · Registered {formatDate(farmer.createdAt)}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button icon={Pencil} onClick={() => setEditOpen(true)}>
            Edit
          </Button>
          <Button
            variant="outline"
            icon={ArrowLeft}
            onClick={() => navigate('/farmers')}
          >
            Back
          </Button>
        </div>
      </div>

      <Card>
        <dl className="grid grid-cols-1 gap-x-6 gap-y-5 sm:grid-cols-2 lg:grid-cols-3">
          {facts.map(({ icon: Icon, label, value }) => (
            <div key={label} className="flex items-start gap-3">
              <span className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground">
                <Icon className="h-4.5 w-4.5" aria-hidden="true" />
              </span>
              <div>
                <dt className="text-xs text-muted-foreground">{label}</dt>
                <dd className="font-medium text-foreground">{value}</dd>
              </div>
            </div>
          ))}
        </dl>
      </Card>

      {/* Financial (DT-77) — balances, statement and payment history from the ledger. */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h2 className="font-heading text-lg font-semibold text-foreground">Financial</h2>
        <Select
          aria-label="Filter by currency"
          placeholder="All currencies"
          value={currency}
          onChange={(e) => setCurrency(e.target.value)}
          options={[
            { label: 'USD', value: 'USD' },
            { label: 'LBP', value: 'LBP' },
          ]}
          containerClassName="sm:w-44"
        />
      </div>

      {financeLoading ? (
        <div className="flex justify-center py-10">
          <LoadingSpinner label="Loading finances…" />
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {balances.length === 0 ? (
              <Card>
                <p className="text-sm text-muted-foreground">No outstanding balance.</p>
              </Card>
            ) : (
              balances.map((b) => (
                <Card key={b.currency}>
                  <p className="text-xs text-muted-foreground">Outstanding ({b.currency})</p>
                  <p className="mt-1 text-2xl font-semibold text-foreground">
                    {formatMoney(b.outstanding, b.currency)}
                  </p>
                </Card>
              ))
            )}
          </div>

          <Card flush>
            <CardHeader title="Statement" description="Settlement credits and payment debits" />
            <Table
              columns={statementColumns}
              rows={statement}
              rowKey={(l) => String(l.id)}
              empty={
                <EmptyState
                  icon={Wallet}
                  title="No financial history"
                  description="Confirmed settlements and payments appear here."
                />
              }
            />
          </Card>

          <Card flush>
            <CardHeader title="Payment history" />
            <Table
              columns={paymentColumns}
              rows={payments}
              rowKey={(p) => String(p.id)}
              empty={<EmptyState icon={Wallet} title="No payments recorded" />}
            />
          </Card>
        </>
      )}

      <FarmerFormModal
        open={editOpen}
        farmer={farmer}
        onClose={() => setEditOpen(false)}
        onSaved={() => setRefreshKey((k) => k + 1)}
      />
    </div>
  )
}
