import { useParams, useNavigate, Link } from 'react-router-dom'
import {
  ArrowLeft,
  Phone,
  MapPin,
  Sprout,
  Wallet,
  Truck,
} from 'lucide-react'
import {
  Card,
  CardHeader,
  Button,
  Table,
  LoadingSpinner,
  EmptyState,
  FarmerStatusBadge,
  DeliveryStatusBadge,
  GradeBadge,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { farmerService } from '@/services/farmerService'
import { deliveryService } from '@/services/deliveryService'
import { formatCurrency, formatDate, formatWeight } from '@/utils/format'
import type { Delivery } from '@/types'

export function FarmerDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const { data: farmer, loading } = useAsync(
    () => farmerService.getById(id),
    [id],
  )
  const { data: deliveries } = useAsync(
    () => deliveryService.listByFarmer(id),
    [id],
  )

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
          <Button variant="outline" icon={ArrowLeft} onClick={() => navigate('/farmers')}>
            Back to Farmers
          </Button>
        }
      />
    )
  }

  const deliveryColumns: Column<Delivery>[] = [
    { header: 'Delivery', cell: (d) => <span className="font-mono text-xs">{d.id}</span> },
    { header: 'Product', cell: (d) => d.product },
    { header: 'Weight', align: 'end', cell: (d) => formatWeight(d.netWeight) },
    { header: 'Grade', align: 'center', cell: (d) => <GradeBadge grade={d.grade} /> },
    { header: 'Date', className: 'hidden sm:table-cell', cell: (d) => formatDate(d.date) },
    { header: 'Status', cell: (d) => <DeliveryStatusBadge status={d.status} /> },
  ]

  const facts = [
    { icon: MapPin, label: 'Location', value: `${farmer.village}, ${farmer.region}` },
    { icon: Phone, label: 'Phone', value: farmer.phone },
    { icon: Sprout, label: 'Main crop', value: farmer.mainCrop },
    { icon: Truck, label: 'Total deliveries', value: String(farmer.totalDeliveries) },
    { icon: Wallet, label: 'Outstanding balance', value: formatCurrency(farmer.balance) },
  ]

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
            {farmer.name
              .split(' ')
              .slice(0, 2)
              .map((n) => n[0])
              .join('')}
          </span>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="font-heading text-xl font-semibold text-foreground sm:text-2xl">
                {farmer.name}
              </h1>
              <FarmerStatusBadge status={farmer.status} />
            </div>
            <p className="mt-0.5 font-mono text-xs text-muted-foreground">
              {farmer.id} · Joined {formatDate(farmer.joinedAt)}
            </p>
          </div>
        </div>
        <Button variant="outline" icon={ArrowLeft} onClick={() => navigate('/farmers')}>
          Back
        </Button>
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

      <Card flush>
        <CardHeader
          title="Delivery History"
          description="All produce delivered by this farmer"
        />
        <Table
          columns={deliveryColumns}
          rows={deliveries ?? []}
          rowKey={(d) => d.id}
          empty={
            <EmptyState
              icon={Truck}
              title="No deliveries yet"
              description="Deliveries from this farmer will appear here."
            />
          }
        />
      </Card>
    </div>
  )
}
