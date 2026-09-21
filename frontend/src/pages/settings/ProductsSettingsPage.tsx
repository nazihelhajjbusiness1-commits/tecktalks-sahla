import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Package, Plus, Pencil, Power } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  Table,
  ActiveBadge,
  EmptyState,
  Alert,
  Modal,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { productService } from '@/services/productService'
import { ProductFormModal } from './ProductFormModal'
import type { Product } from '@/types'

export function ProductsSettingsPage() {
  const navigate = useNavigate()
  const [refreshKey, setRefreshKey] = useState(0)
  const [addOpen, setAddOpen] = useState(false)
  const [editing, setEditing] = useState<Product | null>(null)
  const [toggling, setToggling] = useState<Product | null>(null)
  const [busy, setBusy] = useState(false)

  const { data, loading, error } = useAsync(
    () => productService.list({ size: 100 }),
    [refreshKey],
  )

  const refresh = () => setRefreshKey((k) => k + 1)
  const products = data?.content ?? []

  async function confirmToggle() {
    if (!toggling) return
    setBusy(true)
    try {
      await productService.update(toggling.id, {
        name: toggling.name,
        variety: toggling.variety,
        unit: toggling.unit,
        active: !toggling.active,
      })
      setToggling(null)
      refresh()
    } finally {
      setBusy(false)
    }
  }

  const columns: Column<Product>[] = [
    {
      header: 'Name',
      cell: (p) => <span className="font-medium text-foreground">{p.name}</span>,
    },
    { header: 'Variety', cell: (p) => p.variety },
    {
      header: 'Unit',
      cell: (p) => <span className="text-muted-foreground">{p.unit}</span>,
    },
    { header: 'Status', cell: (p) => <ActiveBadge active={p.active} /> },
    {
      header: 'Actions',
      align: 'end',
      srOnlyHeader: true,
      cell: (p) => (
        <div className="flex justify-end gap-1">
          <Button
            variant="ghost"
            size="sm"
            icon={Pencil}
            aria-label={`Edit ${p.name}`}
            onClick={(e) => {
              e.stopPropagation()
              setEditing(p)
            }}
          >
            Edit
          </Button>
          <Button
            variant="ghost"
            size="sm"
            icon={Power}
            aria-label={`${p.active ? 'Deactivate' : 'Activate'} ${p.name}`}
            onClick={(e) => {
              e.stopPropagation()
              setToggling(p)
            }}
          >
            {p.active ? 'Deactivate' : 'Activate'}
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-1 text-sm text-muted-foreground">
        <Link to="/settings" className="hover:text-foreground hover:underline">
          Settings
        </Link>
        <span aria-hidden="true">/</span>
        <span className="text-foreground">Products</span>
      </div>

      <PageHeader
        title="Products"
        description="Manage the crops and produce your center handles"
        actions={
          <Button icon={Plus} onClick={() => setAddOpen(true)}>
            Add Product
          </Button>
        }
      />

      <Card flush>
        {error ? (
          <div className="p-4">
            <Alert variant="danger" title="Couldn't load products">
              {error}
            </Alert>
          </div>
        ) : (
          <Table
            columns={columns}
            rows={products}
            rowKey={(p) => String(p.id)}
            loading={loading}
            onRowClick={(p) => navigate(`/settings/products/${p.id}`)}
            empty={
              <EmptyState
                icon={Package}
                title="No products yet"
                description="Add your first product to define grades and pricing."
                action={
                  <Button icon={Plus} onClick={() => setAddOpen(true)}>
                    Add Product
                  </Button>
                }
              />
            }
          />
        )}
      </Card>

      <ProductFormModal
        open={addOpen}
        onClose={() => setAddOpen(false)}
        onSaved={refresh}
      />
      <ProductFormModal
        open={editing !== null}
        product={editing}
        onClose={() => setEditing(null)}
        onSaved={refresh}
      />

      <Modal
        open={toggling !== null}
        onClose={() => setToggling(null)}
        title={toggling?.active ? 'Deactivate product' : 'Activate product'}
        description={
          toggling?.active
            ? `“${toggling?.name}” will be hidden from operational use but kept for historical records.`
            : `“${toggling?.name}” will be available again.`
        }
        footer={
          <>
            <Button variant="outline" onClick={() => setToggling(null)} disabled={busy}>
              Cancel
            </Button>
            <Button
              variant={toggling?.active ? 'danger' : 'primary'}
              loading={busy}
              onClick={confirmToggle}
            >
              {toggling?.active ? 'Deactivate' : 'Activate'}
            </Button>
          </>
        }
      />
    </div>
  )
}
