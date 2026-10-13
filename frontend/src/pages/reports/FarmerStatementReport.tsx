import { useEffect, useState } from 'react'
import {
  Card,
  Select,
  Input,
  Table,
  Alert,
  EmptyState,
  type Column,
  type SelectOption,
} from '@/components/common'
import { UserRound } from 'lucide-react'
import { useAsync } from '@/hooks/useAsync'
import { reportService } from '@/services/reportService'
import { farmerService } from '@/services/farmerService'
import { formatDateTime, formatMoney } from '@/utils/format'
import type { Currency, Farmer, StatementLine } from '@/types'

export function FarmerStatementReport({ initialFarmerId }: { initialFarmerId?: number }) {
  const [farmers, setFarmers] = useState<Farmer[]>([])
  const [farmerId, setFarmerId] = useState(initialFarmerId ? String(initialFarmerId) : '')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')

  useEffect(() => {
    farmerService.list({ size: 100 }).then((p) => setFarmers(p.content)).catch(() => {})
  }, [])

  useEffect(() => {
    if (initialFarmerId) setFarmerId(String(initialFarmerId))
  }, [initialFarmerId])

  const { data, loading, error } = useAsync(
    () =>
      farmerId
        ? reportService.farmerStatement(Number(farmerId), {
            from: from || undefined,
            to: to || undefined,
          })
        : Promise.resolve<StatementLine[]>([]),
    [farmerId, from, to],
  )

  const farmerOptions: SelectOption[] = [
    { label: 'Select a farmer…', value: '' },
    ...farmers.map((f) => ({ label: `${f.name} (${f.farmerCode})`, value: String(f.id) })),
  ]

  const money = (value: number | null, currency: Currency) =>
    value == null ? '—' : formatMoney(value, currency)

  const columns: Column<StatementLine>[] = [
    { header: 'Date', cell: (r) => formatDateTime(r.occurredAt) },
    { header: 'Type', cell: (r) => r.entryType.replace(/_/g, ' ') },
    { header: 'Description', cell: (r) => r.description ?? '—' },
    { header: 'Debit', align: 'end', cell: (r) => money(r.debit, r.currency) },
    { header: 'Credit', align: 'end', cell: (r) => money(r.credit, r.currency) },
    {
      header: 'Balance',
      align: 'end',
      cell: (r) => <span className="font-semibold">{formatMoney(r.runningBalance, r.currency)}</span>,
    },
  ]

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <Select
          label="Farmer"
          options={farmerOptions}
          value={farmerId}
          onChange={(e) => setFarmerId(e.target.value)}
        />
        <Input label="From" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        <Input label="To" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
      </div>

      {error && (
        <Alert variant="danger" title="Couldn't load statement">
          {error}
        </Alert>
      )}

      <Card flush>
        {!farmerId ? (
          <div className="py-6">
            <EmptyState
              icon={UserRound}
              title="Choose a farmer"
              description="Select a farmer to see their chronological statement."
            />
          </div>
        ) : (
          <Table
            columns={columns}
            rows={data ?? []}
            rowKey={(r) => String(r.id)}
            loading={loading}
            empty={
              <EmptyState
                icon={UserRound}
                title="No entries"
                description="This farmer has no financial entries for the selected range."
              />
            }
          />
        )}
      </Card>
    </div>
  )
}
