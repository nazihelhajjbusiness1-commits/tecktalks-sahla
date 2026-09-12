import { useNavigate } from 'react-router-dom'
import {
  Truck,
  Scale,
  Users,
  Wallet,
  Boxes,
  ArrowRight,
  CircleDollarSign,
  CircleDashed,
  Clock,
} from 'lucide-react'
import {
  Card,
  CardHeader,
  StatCard,
  Table,
  Button,
  DeliveryStatusBadge,
  GradeBadge,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { dashboardService } from '@/services/dashboardService'
import { useAuth } from '@/hooks/useAuth'
import {
  formatCurrency,
  formatNumber,
  formatWeight,
} from '@/utils/format'
import type { Delivery, InventorySummaryRow } from '@/types'

export function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { data, loading } = useAsync(() => dashboardService.load())

  const deliveryColumns: Column<Delivery>[] = [
    { header: 'Farmer', cell: (d) => <span className="font-medium">{d.farmerName}</span> },
    { header: 'Product', cell: (d) => d.product },
    { header: 'Weight', align: 'end', cell: (d) => formatWeight(d.netWeight) },
    { header: 'Grade', align: 'center', cell: (d) => <GradeBadge grade={d.grade} /> },
    { header: 'Status', cell: (d) => <DeliveryStatusBadge status={d.status} /> },
  ]

  const maxInventory =
    data?.inventorySummary.reduce((m, r) => Math.max(m, r.quantityKg), 0) ?? 0

  const paymentsTotal = data
    ? data.paymentsBreakdown.paid +
      data.paymentsBreakdown.partial +
      data.paymentsBreakdown.pending
    : 0

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

      {/* Summary cards */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-5">
        <StatCard
          label="Today's Deliveries"
          value={loading ? '—' : formatNumber(data!.stats.todaysDeliveries)}
          icon={Truck}
          tone="primary"
          trend={{ value: '+4', direction: 'up' }}
          caption="vs yesterday"
        />
        <StatCard
          label="Produce Received"
          value={loading ? '—' : formatWeight(data!.stats.produceReceivedKg)}
          icon={Scale}
          tone="info"
          caption="today"
        />
        <StatCard
          label="Active Farmers"
          value={loading ? '—' : formatNumber(data!.stats.activeFarmers)}
          icon={Users}
          tone="success"
        />
        <StatCard
          label="Payments Due"
          value={loading ? '—' : formatCurrency(data!.stats.paymentsDueUsd)}
          icon={Wallet}
          tone="accent"
          caption="outstanding"
        />
        <StatCard
          label="Inventory"
          value={loading ? '—' : formatWeight(data!.stats.inventoryKg)}
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
            rowKey={(d) => d.id}
            loading={loading}
            onRowClick={() => navigate('/deliveries')}
          />
        </Card>

        {/* Payments overview */}
        <Card flush>
          <CardHeader
            title="Payments Overview"
            description="Farmer payment status"
          />
          <div className="space-y-4 p-5">
            {data && (
              <>
                <PaymentBar
                  label="Paid"
                  count={data.paymentsBreakdown.paid}
                  total={paymentsTotal}
                  icon={CircleDollarSign}
                  barClass="bg-success"
                  tintClass="text-success"
                />
                <PaymentBar
                  label="Partially Paid"
                  count={data.paymentsBreakdown.partial}
                  total={paymentsTotal}
                  icon={CircleDashed}
                  barClass="bg-warning"
                  tintClass="text-warning"
                />
                <PaymentBar
                  label="Pending"
                  count={data.paymentsBreakdown.pending}
                  total={paymentsTotal}
                  icon={Clock}
                  barClass="bg-neutral-status"
                  tintClass="text-neutral-status"
                />
              </>
            )}
            <Button
              variant="outline"
              block
              icon={Wallet}
              onClick={() => navigate('/payments')}
            >
              Go to Payments
            </Button>
          </div>
        </Card>
      </div>

      {/* Inventory overview */}
      <Card flush>
        <CardHeader
          title="Inventory Overview"
          description="Top stock on hand by product and grade"
          action={
            <Button
              variant="ghost"
              size="sm"
              icon={ArrowRight}
              onClick={() => navigate('/inventory')}
            >
              View all
            </Button>
          }
        />
        <div className="grid grid-cols-1 gap-x-8 gap-y-4 p-5 sm:grid-cols-2">
          {data?.inventorySummary.map((row) => (
            <InventoryRow key={row.label} row={row} max={maxInventory} />
          ))}
        </div>
      </Card>
    </div>
  )
}

function PaymentBar({
  label,
  count,
  total,
  icon: Icon,
  barClass,
  tintClass,
}: {
  label: string
  count: number
  total: number
  icon: typeof Clock
  barClass: string
  tintClass: string
}) {
  const pct = total > 0 ? Math.round((count / total) * 100) : 0
  return (
    <div>
      <div className="mb-1.5 flex items-center justify-between text-sm">
        <span className="flex items-center gap-2 font-medium text-foreground">
          <Icon className={`h-4 w-4 ${tintClass}`} aria-hidden="true" />
          {label}
        </span>
        <span className="text-muted-foreground">
          {count} · {pct}%
        </span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-muted">
        <div
          className={`h-full rounded-full ${barClass}`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

function InventoryRow({
  row,
  max,
}: {
  row: InventorySummaryRow
  max: number
}) {
  const pct = max > 0 ? Math.round((row.quantityKg / max) * 100) : 0
  return (
    <div>
      <div className="mb-1.5 flex items-center justify-between text-sm">
        <span className="font-medium text-foreground">{row.label}</span>
        <span className="text-muted-foreground">{formatWeight(row.quantityKg)}</span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-muted">
        <div
          className="h-full rounded-full bg-primary/80"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}
