import type { LucideIcon } from 'lucide-react'
import {
  CheckCircle2,
  Circle,
  Clock,
  FileText,
  Scale,
  ClipboardCheck,
  ShieldCheck,
  XCircle,
  PackageCheck,
  PackageMinus,
  PackageX,
  CircleDollarSign,
  CircleDashed,
} from 'lucide-react'
import { cn } from '@/utils/cn'
import type {
  DeliveryStatus,
  FarmerStatus,
  InventoryStatus,
  PaymentStatus,
} from '@/types'

type Tone = 'success' | 'warning' | 'info' | 'danger' | 'neutral' | 'primary'

const toneClasses: Record<Tone, string> = {
  success: 'bg-success-soft text-success',
  warning: 'bg-warning-soft text-warning',
  info: 'bg-info-soft text-info',
  danger: 'bg-danger-soft text-danger',
  neutral: 'bg-neutral-status-soft text-neutral-status',
  primary: 'bg-primary-soft text-primary',
}

interface BadgeConfig {
  label: string
  tone: Tone
  icon: LucideIcon
}

// Color is never the sole signal — every badge carries an icon + text label.
const deliveryConfig: Record<DeliveryStatus, BadgeConfig> = {
  draft: { label: 'Draft', tone: 'neutral', icon: FileText },
  weighed: { label: 'Weighed', tone: 'info', icon: Scale },
  grading: { label: 'Grading', tone: 'warning', icon: ClipboardCheck },
  confirmed: { label: 'Confirmed', tone: 'primary', icon: ShieldCheck },
  completed: { label: 'Completed', tone: 'success', icon: CheckCircle2 },
  rejected: { label: 'Rejected', tone: 'danger', icon: XCircle },
}

const paymentConfig: Record<PaymentStatus, BadgeConfig> = {
  paid: { label: 'Paid', tone: 'success', icon: CircleDollarSign },
  partial: { label: 'Partially Paid', tone: 'warning', icon: CircleDashed },
  pending: { label: 'Pending', tone: 'neutral', icon: Clock },
}

const inventoryConfig: Record<InventoryStatus, BadgeConfig> = {
  in_stock: { label: 'In Stock', tone: 'success', icon: PackageCheck },
  low_stock: { label: 'Low Stock', tone: 'warning', icon: PackageMinus },
  out_of_stock: { label: 'Out of Stock', tone: 'danger', icon: PackageX },
}

const farmerConfig: Record<FarmerStatus, BadgeConfig> = {
  active: { label: 'Active', tone: 'success', icon: CheckCircle2 },
  inactive: { label: 'Inactive', tone: 'neutral', icon: Circle },
}

function Badge({ config }: { config: BadgeConfig }) {
  const { label, tone, icon: Icon } = config
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium whitespace-nowrap',
        toneClasses[tone],
      )}
    >
      <Icon className="h-3.5 w-3.5" aria-hidden="true" />
      {label}
    </span>
  )
}

export function DeliveryStatusBadge({ status }: { status: DeliveryStatus }) {
  return <Badge config={deliveryConfig[status]} />
}

export function PaymentStatusBadge({ status }: { status: PaymentStatus }) {
  return <Badge config={paymentConfig[status]} />
}

export function InventoryStatusBadge({ status }: { status: InventoryStatus }) {
  return <Badge config={inventoryConfig[status]} />
}

export function FarmerStatusBadge({ status }: { status: FarmerStatus }) {
  return <Badge config={farmerConfig[status]} />
}

const gradeClasses: Record<string, string> = {
  A: 'bg-success-soft text-success',
  B: 'bg-info-soft text-info',
  C: 'bg-warning-soft text-warning',
  Ungraded: 'bg-neutral-status-soft text-neutral-status',
}

/** Compact quality-grade pill (A / B / C / Ungraded). */
export function GradeBadge({ grade }: { grade: string }) {
  return (
    <span
      className={cn(
        'inline-flex min-w-7 items-center justify-center rounded-md px-1.5 py-0.5 text-xs font-semibold',
        gradeClasses[grade] ?? gradeClasses.Ungraded,
      )}
    >
      {grade === 'Ungraded' ? '—' : grade}
    </span>
  )
}
