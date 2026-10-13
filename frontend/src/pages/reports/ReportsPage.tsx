import { useState } from 'react'
import { ClipboardList, UserRound, Boxes, Wallet, type LucideIcon } from 'lucide-react'
import { PageHeader } from '@/components/common'
import { cn } from '@/utils/cn'
import { DailyCollectionReport } from './DailyCollectionReport'
import { FarmerStatementReport } from './FarmerStatementReport'
import { InventoryReport } from './InventoryReport'
import { OutstandingPaymentsReport } from './OutstandingPaymentsReport'

type ReportKey = 'daily' | 'statement' | 'inventory' | 'payments'

interface Tab {
  key: ReportKey
  label: string
  icon: LucideIcon
}

const tabs: Tab[] = [
  { key: 'daily', label: 'Daily Collection', icon: ClipboardList },
  { key: 'statement', label: 'Farmer Statement', icon: UserRound },
  { key: 'inventory', label: 'Inventory', icon: Boxes },
  { key: 'payments', label: 'Outstanding Payments', icon: Wallet },
]

export function ReportsPage() {
  const [active, setActive] = useState<ReportKey>('daily')
  const [statementFarmerId, setStatementFarmerId] = useState<number | undefined>(undefined)

  const openStatement = (farmerId: number) => {
    setStatementFarmerId(farmerId)
    setActive('statement')
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Reports"
        description="Operational and financial reports for the cooperative"
      />

      <div className="flex flex-wrap gap-2 border-b border-border" role="tablist">
        {tabs.map((tab) => {
          const Icon = tab.icon
          const selected = active === tab.key
          return (
            <button
              key={tab.key}
              type="button"
              role="tab"
              aria-selected={selected}
              onClick={() => setActive(tab.key)}
              className={cn(
                'inline-flex items-center gap-2 border-b-2 px-3 py-2.5 text-sm font-medium transition-colors -mb-px',
                selected
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:text-foreground',
              )}
            >
              <Icon className="h-4 w-4" aria-hidden="true" />
              {tab.label}
            </button>
          )
        })}
      </div>

      {active === 'daily' && <DailyCollectionReport />}
      {active === 'statement' && (
        <FarmerStatementReport initialFarmerId={statementFarmerId} />
      )}
      {active === 'inventory' && <InventoryReport />}
      {active === 'payments' && (
        <OutstandingPaymentsReport onViewStatement={openStatement} />
      )}
    </div>
  )
}
