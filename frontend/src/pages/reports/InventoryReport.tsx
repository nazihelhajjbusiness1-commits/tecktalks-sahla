import { useEffect, useState } from 'react'
import {
  Card,
  Select,
  Table,
  Alert,
  EmptyState,
  type Column,
  type SelectOption,
} from '@/components/common'
import { Boxes } from 'lucide-react'
import { useAsync } from '@/hooks/useAsync'
import { reportService } from '@/services/reportService'
import { productService } from '@/services/productService'
import { formatWeight } from '@/utils/format'
import type { InventoryReportRow, Product } from '@/types'

export function InventoryReport() {
  const [productId, setProductId] = useState('')
  const [products, setProducts] = useState<Product[]>([])

  useEffect(() => {
    productService.list({ size: 100 }).then((p) => setProducts(p.content)).catch(() => {})
  }, [])

  const { data, loading, error } = useAsync(
    () => reportService.inventory({ productId: productId ? Number(productId) : undefined }),
    [productId],
  )

  const productOptions: SelectOption[] = [
    { label: 'All products', value: '' },
    ...products.map((p) => ({ label: `${p.name} · ${p.variety}`, value: String(p.id) })),
  ]

  const columns: Column<InventoryReportRow>[] = [
    { header: 'Product', cell: (r) => <span className="font-medium">{r.productName ?? '—'}</span> },
    { header: 'Grade', cell: (r) => r.gradeName ?? r.gradeCode ?? 'Ungraded' },
    {
      header: 'Available quantity',
      align: 'end',
      cell: (r) => <span className="font-semibold">{formatWeight(r.quantity)}</span>,
    },
  ]

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 gap-3 sm:max-w-sm">
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
          rowKey={(r) => `${r.productId}-${r.gradeId ?? 'x'}`}
          loading={loading}
          empty={
            <EmptyState
              icon={Boxes}
              title="No stock on hand"
              description="Confirmed deliveries will add stock here."
            />
          }
        />
      </Card>
    </div>
  )
}
