import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import {
  ArrowLeft,
  Phone,
  MapPin,
  Hash,
  CalendarDays,
  Pencil,
  Truck,
  Wallet,
} from 'lucide-react'
import {
  Card,
  CardHeader,
  Button,
  LoadingSpinner,
  EmptyState,
  FarmerStatusBadge,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { farmerService } from '@/services/farmerService'
import { formatDate, initials } from '@/utils/format'
import { FarmerFormModal } from './FarmerFormModal'

export function FarmerDetailPage() {
  const { id = '' } = useParams()
  const farmerId = Number(id)
  const navigate = useNavigate()
  const [editOpen, setEditOpen] = useState(false)
  const [refreshKey, setRefreshKey] = useState(0)

  const { data: farmer, loading } = useAsync(
    () => farmerService.getById(farmerId),
    [farmerId, refreshKey],
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

      {/* Future workflows — intentionally placeholders in Sprint 2. */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card flush>
          <CardHeader
            title="Deliveries"
            description="Produce delivered by this farmer"
          />
          <EmptyState
            icon={Truck}
            title="Coming soon"
            description="Delivery intake and history arrive in a later sprint."
          />
        </Card>
        <Card flush>
          <CardHeader
            title="Payments"
            description="Settlements and payment history"
          />
          <EmptyState
            icon={Wallet}
            title="Coming soon"
            description="Farmer settlements and payments arrive in a later sprint."
          />
        </Card>
      </div>

      <FarmerFormModal
        open={editOpen}
        farmer={farmer}
        onClose={() => setEditOpen(false)}
        onSaved={() => setRefreshKey((k) => k + 1)}
      />
    </div>
  )
}
