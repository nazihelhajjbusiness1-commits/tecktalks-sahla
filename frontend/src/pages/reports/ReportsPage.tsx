import { useState } from 'react'
import {
  ClipboardList,
  UserRound,
  Boxes,
  Wallet,
  Download,
  type LucideIcon,
} from 'lucide-react'
import { PageHeader, Card, Button, Modal, Alert } from '@/components/common'

interface ReportDef {
  key: string
  title: string
  description: string
  icon: LucideIcon
  tone: string
}

const reports: ReportDef[] = [
  {
    key: 'daily',
    title: 'Daily Collection Report',
    description: 'Everything received today — deliveries, weights, and grades.',
    icon: ClipboardList,
    tone: 'bg-primary-soft text-primary',
  },
  {
    key: 'farmer',
    title: 'Farmer Statement',
    description: 'Per-farmer deliveries, balances, and payment history.',
    icon: UserRound,
    tone: 'bg-info-soft text-info',
  },
  {
    key: 'inventory',
    title: 'Inventory Report',
    description: 'Current stock levels by product and quality grade.',
    icon: Boxes,
    tone: 'bg-success-soft text-success',
  },
  {
    key: 'payments',
    title: 'Outstanding Payments Report',
    description: 'Amounts owed to farmers, grouped by payment status.',
    icon: Wallet,
    tone: 'bg-accent-soft text-accent',
  },
]

export function ReportsPage() {
  const [active, setActive] = useState<ReportDef | null>(null)

  return (
    <div className="space-y-6">
      <PageHeader
        title="Reports"
        description="Generate operational reports for the cooperative"
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {reports.map((report) => {
          const Icon = report.icon
          return (
            <Card key={report.key} className="flex flex-col">
              <div className="flex items-start gap-4">
                <span
                  className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-md ${report.tone}`}
                >
                  <Icon className="h-5.5 w-5.5" aria-hidden="true" />
                </span>
                <div className="min-w-0">
                  <h2 className="font-heading text-base font-semibold text-foreground">
                    {report.title}
                  </h2>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {report.description}
                  </p>
                </div>
              </div>
              <div className="mt-4 flex justify-end">
                <Button
                  variant="outline"
                  size="sm"
                  icon={Download}
                  onClick={() => setActive(report)}
                >
                  Generate
                </Button>
              </div>
            </Card>
          )
        })}
      </div>

      <Modal
        open={active !== null}
        onClose={() => setActive(null)}
        title={active?.title ?? 'Report'}
        footer={
          <Button variant="outline" onClick={() => setActive(null)}>
            Close
          </Button>
        }
      >
        <Alert variant="info" title="Report generation coming soon">
          Building and exporting reports (PDF / spreadsheet) will be wired to the
          backend in a later week. The cards and flow are ready.
        </Alert>
      </Modal>
    </div>
  )
}
