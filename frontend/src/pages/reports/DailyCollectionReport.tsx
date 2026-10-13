import { useEffect, useState } from 'react'
import {
  Card,
  Input,
  Select,
  Table,
  Alert,
  EmptyState,
  type Column,
  type SelectOption,
} from '@/components/common'
import { ClipboardList } from 'lucide-react'
import { useAsync } from '@/hooks/useAsync'
import { reportService } from '@/services/reportService'
import { productService } from '@/services/productService'
import { formatDate, formatMoney, formatWeight } from '@/utils/format'
import type { DailyCollectionRow, Product } from '@/types'

export function DailyCollectionReport() {
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [productId, setProductId] = useState('')
  const [products, setProducts] = useState<Product[]>([])

  useEffect(() => {
    productService.list({ size: 100 }).then((p) => setProducts(p.content)).catch(() => {})
  }, [])

  const { data, loading, error } = useAsync(
    () =>
      reportService.dailyCollection({
        from: from || undefined,
        to: to || undefined,
        productId: productId ? Number(productId) : undefined,
      }),
    [from, to, productId],
  )

  const productOptions: SelectOption[] = [
    { label: 'All products', value: '' },
    ...products.map((p) => ({ label: `${p.name} · ${p.variety}`, value: String(p.id) })),
  ]

  const columns: Column<DailyCollectionRow>[] = [
    { header: 'Date', cell: (r) => formatDate(r.deliveryDate) },
    { header: 'Farmer', cell: (r) => <span className="font-medium">{r.farmerName ?? '—'}</span> },
    { header: 'Product', cell: (r) => r.productName ?? '—' },
    { header: 'Grade', cell: (r) => r.gradeName ?? r.gradeCode ?? '—' },
    { header: 'Net weight', align: 'end', cell: (r) => formatWeight(r.netWeight) },
    { header: 'Unit price', align: 'end', cell: (r) => formatMoney(r.unitPrice, r.currency) },
    {
      header: 'Value',
      align: 'end',
      cell: (r) => (
        <span className="font-semibold">{formatMoney(r.deliveryValue, r.currency)}</span>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <Input label="From" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        <Input label="To" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        <Select
          label="Product"
          options={productOptions}
          value={productId}
          onChange={(e) => setProductId(e.target.value)}
        />
      </div>

      {error && (
        <Alert variant="danger" title="Couldn't load report">
          {error}
        </Alert>
      )}

      <Card flush>
        <Table
          columns={columns}
          rows={data ?? []}
          rowKey={(r) => `${r.farmerId}-${r.productId}-${r.deliveryDate}-${r.gradeId ?? 'x'}`}
          loading={loading}
          empty={
            <EmptyState
              icon={ClipboardList}
              title="No collections found"
              description="No confirmed deliveries match these filters."
            />
          }
        />
      </Card>
    </div>
  )
}
