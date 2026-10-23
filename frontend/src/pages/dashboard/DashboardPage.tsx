import { useNavigate } from 'react-router-dom'
import {
  Truck,
  Scale,
  Users,
  Wallet,
  Boxes,
  ArrowRight,
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
