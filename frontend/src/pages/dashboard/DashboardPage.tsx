import { useNavigate } from 'react-router-dom'
import {
  Truck,
  Scale,
  Users,
  Wallet,
  Boxes,
  ArrowRight,
  PackagePlus,
  UserPlus,
  CreditCard,
  AlertTriangle,
  CheckCircle2,
  ChevronRight,
  type LucideIcon,
} from 'lucide-react'
import {
  Card,
  CardHeader,
  StatCard,
  Table,
  Button,
  Alert,
  EmptyState,
  IntakeStatusBadge,
  type Column,
} from '@/components/common'
import { IntakeChart } from '@/components/dashboard/IntakeChart'
import { useAsync } from '@/hooks/useAsync'
import { dashboardService } from '@/services/dashboardService'
import { useAuth } from '@/hooks/useAuth'
import {
  formatCurrency,
  formatDate,
  formatMoney,
  formatNumber,
  formatWeight,
} from '@/utils/format'
import type {
  Currency,
  DashboardRecentDelivery,
  DashboardRecentPayment,
  IntakeStatus,
} from '@/types'

/** Picks the amount owed in a given currency (0 when the farmer owes nothing). */
function owedIn(
  rows: { currency: Currency; amount: number }[] | undefined,
  currency: Currency,
): number {
  return rows?.find((r) => r.currency === currency)?.amount ?? 0
}

export function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { data, loading, error } = useAsync(() => dashboardService.load())

  const deliveryColumns: Column<DashboardRecentDelivery>[] = [
    {
      header: 'Farmer',
      cell: (d) => <span className="font-medium">{d.farmerName ?? '—'}</span>,
    },
    { header: 'Product', cell: (d) => d.productName ?? '—' },
    {
      header: 'Weight',
      align: 'end',
      cell: (d) => `${formatNumber(d.quantity)} ${d.unit}`,
    },
    {
      header: 'Status',
      cell: (d) => <IntakeStatusBadge status={d.status as IntakeStatus} />,
    },
    { header: 'Date', align: 'end', cell: (d) => formatDate(d.deliveryDate) },
  ]

  const quickActions: {
    label: string
    hint: string
    icon: LucideIcon
    to: string
  }[] = [
    { label: 'Record Delivery', hint: 'Log produce received', icon: PackagePlus, to: '/deliveries' },
    { label: 'Add Farmer', hint: 'Register a supplier', icon: UserPlus, to: '/farmers' },
    { label: 'Record Payment', hint: 'Settle with a farmer', icon: CreditCard, to: '/payments' },
  ]

  // "Needs attention" is derived from the fields the dashboard API exposes.
  const owed = data?.amountOwedToFarmers.filter((a) => a.amount > 0) ?? []
  const noPaymentsToday = !!data && data.paymentsToday.length === 0
  const attentionCount = owed.length + (noPaymentsToday ? 1 : 0)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-heading text-xl font-semibold tracking-tight text-foreground sm:text-2xl">
          Good day, {user?.name?.split(' ')[0] ?? 'there'}
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Here's what's happening at {user?.cooperative} today.
        </p>
      </div>

      {error && (
        <Alert variant="danger" title="Couldn't load dashboard">
          {error}
        </Alert>
      )}

      {/* Quick actions — the tasks an operator lands here to do. */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {quickActions.map((a) => (
          <button
            key={a.to}
            type="button"
            onClick={() => navigate(a.to)}
            className="lift flex items-center gap-3 rounded-lg border border-border bg-card p-4 text-start shadow-card cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-primary-soft text-primary">
              <a.icon className="h-5 w-5" aria-hidden="true" />
            </span>
            <span className="min-w-0">
              <span className="block text-sm font-semibold text-foreground">
                {a.label}
              </span>
              <span className="block text-xs text-muted-foreground">{a.hint}</span>
            </span>
            <ChevronRight
              className="ms-auto h-4 w-4 shrink-0 text-muted-foreground"
              aria-hidden="true"
            />
          </button>
        ))}
      </div>

      {/* Summary cards — all values come from the backend, never hardcoded. */}
      <div className="rise-children grid grid-cols-2 gap-4 lg:grid-cols-5">
        <StatCard
          label="Today's Deliveries"
          value={loading || !data ? '—' : formatNumber(data.todaysDeliveries)}
          icon={Truck}
          tone="primary"
        />
        <StatCard
          label="Produce Received"
          value={loading || !data ? '—' : formatWeight(data.produceReceivedTodayKg)}
          icon={Scale}
          tone="info"
          caption="today"
        />
        <StatCard
          label="Active Farmers"
          value={loading || !data ? '—' : formatNumber(data.activeFarmers)}
          icon={Users}
          tone="success"
        />
        <StatCard
          label="Owed to Farmers"
          value={
            loading || !data
              ? '—'
              : formatCurrency(owedIn(data.amountOwedToFarmers, 'USD'))
          }
          icon={Wallet}
          tone="accent"
          caption="outstanding (USD)"
        />
        <StatCard
          label="Inventory"
          value={loading || !data ? '—' : formatWeight(data.currentInventoryKg)}
          icon={Boxes}
          tone="neutral"
          caption="in stock"
        />
      </div>

      {/* Intake trend + triage */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        <div className="xl:col-span-2">
          <IntakeChart deliveries={data?.recentDeliveries ?? []} loading={loading} />
        </div>

        <Card flush>
          <CardHeader
            title="Needs Attention"
            description="Items to review"
            action={
              attentionCount > 0 ? (
                <span className="rounded-full bg-warning-soft px-2 py-0.5 text-xs font-medium text-warning">
                  {attentionCount}
                </span>
              ) : undefined
            }
          />
          <div className="p-5">
            {loading ? (
              <p className="text-sm text-muted-foreground">Loading…</p>
            ) : attentionCount === 0 ? (
              <EmptyState
                icon={CheckCircle2}
                title="All caught up"
                description="Nothing needs your attention right now."
              />
            ) : (
              <ul className="space-y-3">
                {owed.map((a) => (
                  <li key={a.currency}>
                    <button
                      type="button"
                      onClick={() => navigate('/payments')}
                      className="flex w-full items-center gap-3 rounded-md border border-warning/20 bg-warning-soft/50 p-3 text-start transition-colors hover:bg-warning-soft cursor-pointer"
                    >
                      <AlertTriangle
                        className="h-5 w-5 shrink-0 text-warning"
                        aria-hidden="true"
                      />
                      <span className="min-w-0 flex-1">
                        <span className="block text-sm font-medium text-foreground">
                          Outstanding to farmers
                        </span>
                        <span className="block text-xs text-muted-foreground">
                          {formatMoney(a.amount, a.currency)} awaiting payment
                        </span>
                      </span>
                      <ChevronRight
                        className="h-4 w-4 shrink-0 text-muted-foreground"
                        aria-hidden="true"
                      />
                    </button>
                  </li>
                ))}
                {noPaymentsToday && (
                  <li>
                    <div className="flex items-center gap-3 rounded-md border border-border bg-muted/50 p-3">
                      <CreditCard
                        className="h-5 w-5 shrink-0 text-muted-foreground"
                        aria-hidden="true"
                      />
                      <span className="min-w-0 flex-1">
                        <span className="block text-sm font-medium text-foreground">
                          No payments recorded today
                        </span>
                        <span className="block text-xs text-muted-foreground">
                          Record payments as you settle with farmers.
                        </span>
                      </span>
                    </div>
                  </li>
                )}
              </ul>
            )}
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {/* Recent deliveries */}
        <Card flush className="xl:col-span-2">
          <CardHeader
            title="Recent Deliveries"
            description="Latest produce received at the center"
            action={
              <Button
                variant="ghost"
                size="sm"
                icon={ArrowRight}
                onClick={() => navigate('/deliveries')}
              >
                View all
              </Button>
            }
          />
          <Table
            columns={deliveryColumns}
            rows={data?.recentDeliveries ?? []}
            rowKey={(d) => String(d.id)}
            loading={loading}
            empty={
              <EmptyState
                icon={Truck}
                title="No deliveries yet"
                description="Recorded deliveries will show here."
              />
            }
          />
        </Card>

        {/* Recent payments */}
        <Card flush>
          <CardHeader title="Recent Payments" description="Latest payments to farmers" />
          <div className="p-5">
            {loading ? (
              <p className="text-sm text-muted-foreground">Loading…</p>
            ) : data && data.recentPayments.length > 0 ? (
              <ul className="divide-y divide-border">
                {data.recentPayments.map((p: DashboardRecentPayment) => (
                  <li key={p.id} className="flex items-center justify-between py-2.5">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-foreground">
                        {p.farmerName ?? '—'}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {formatDate(p.paidAt)}
                      </p>
                    </div>
                    <span className="text-sm font-semibold text-foreground">
                      {formatMoney(p.amount, p.currency)}
                    </span>
                  </li>
                ))}
              </ul>
            ) : (
              <EmptyState
                icon={Wallet}
                title="No payments yet"
                description="Payments to farmers will appear here."
              />
            )}
            <Button
              variant="outline"
              block
              icon={Wallet}
              className="mt-4"
              onClick={() => navigate('/payments')}
            >
              Go to Payments
            </Button>
          </div>
        </Card>
      </div>
    </div>
  )
}
