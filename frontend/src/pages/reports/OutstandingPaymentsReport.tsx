import { Card, Table, Alert, EmptyState, Button, type Column } from '@/components/common'
import { Wallet, FileText } from 'lucide-react'
import { useAsync } from '@/hooks/useAsync'
import { reportService } from '@/services/reportService'
import { formatDate, formatMoney } from '@/utils/format'
import type { OutstandingPaymentsRow } from '@/types'

export function OutstandingPaymentsReport({
  onViewStatement,
}: {
  onViewStatement?: (farmerId: number) => void
}) {
  const { data, loading, error } = useAsync(() => reportService.outstandingPayments())

  const columns: Column<OutstandingPaymentsRow>[] = [
    { header: 'Farmer', cell: (r) => <span className="font-medium">{r.farmerName ?? '—'}</span> },
    { header: 'Code', cell: (r) => r.farmerCode ?? '—' },
    { header: 'Owed', align: 'end', cell: (r) => formatMoney(r.amountOwed, r.currency) },
    { header: 'Paid', align: 'end', cell: (r) => formatMoney(r.amountPaid, r.currency) },
    {
      header: 'Remaining',
      align: 'end',
      cell: (r) => (
        <span className="font-semibold text-accent">{formatMoney(r.remaining, r.currency)}</span>
      ),
    },
    {
      header: 'Last payment',
      align: 'end',
      cell: (r) => (r.lastPaymentAt ? formatDate(r.lastPaymentAt) : '—'),
    },
    {
      header: 'Statement',
      align: 'end',
      srOnlyHeader: true,
      cell: (r) =>
        onViewStatement ? (
          <Button
            variant="ghost"
            size="sm"
            icon={FileText}
            onClick={() => onViewStatement(r.farmerId)}
          >
            Statement
          </Button>
        ) : null,
    },
  ]

  return (
    <div className="space-y-4">
      {error && (
        <Alert variant="danger" title="Couldn't load report">
          {error}
        </Alert>
      )}

      <Card flush>
        <Table
          columns={columns}
          rows={data ?? []}
          rowKey={(r) => `${r.farmerId}-${r.currency}`}
          loading={loading}
          empty={
            <EmptyState
              icon={Wallet}
              title="Everyone is paid up"
              description="No farmer currently has an outstanding balance."
            />
          }
        />
      </Card>
    </div>
  )
}
