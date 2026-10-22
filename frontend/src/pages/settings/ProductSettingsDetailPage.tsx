import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Plus, Pencil, Star, DollarSign } from 'lucide-react'
import {
  Card,
  Button,
  Table,
  ActiveBadge,
  EmptyState,
  LoadingSpinner,
  Alert,
  type Column,
} from '@/components/common'
import { cn } from '@/utils/cn'
import { formatDate } from '@/utils/format'
import { useAsync } from '@/hooks/useAsync'
import { productService } from '@/services/productService'
import { gradeService } from '@/services/gradeService'
import { pricingService } from '@/services/pricingService'
import { GradeFormModal } from './GradeFormModal'
import { PriceFormModal } from './PriceFormModal'
import type { Currency, GradeDefinition, PriceRule } from '@/types'

type Tab = 'grades' | 'pricing'

function formatMoney(amount: number, currency: Currency): string {
  if (currency === 'USD') {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(amount)
  }
  return `${new Intl.NumberFormat('en-US').format(amount)} LBP`
}

export function ProductSettingsDetailPage() {
  const { id = '' } = useParams()
  const productId = Number(id)
  const navigate = useNavigate()

  const [tab, setTab] = useState<Tab>('grades')
  const [refreshKey, setRefreshKey] = useState(0)
  const [gradeModal, setGradeModal] = useState<{ open: boolean; grade: GradeDefinition | null }>({ open: false, grade: null })
  const [priceModal, setPriceModal] = useState<{ open: boolean; price: PriceRule | null }>({ open: false, price: null })

  const { data, loading, error } = useAsync(async () => {
    const [product, grades, prices] = await Promise.all([
      productService.getById(productId),
      gradeService.listByProduct(productId),
      pricingService.listByProduct(productId),
    ])
    return { product, grades, prices }
  }, [productId, refreshKey])

  const refresh = () => setRefreshKey((k) => k + 1)

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner label="Loading product…" />
      </div>
    )
  }

  if (error || !data) {
    return (
      <EmptyState
        title="Product not found"
        description={error ?? 'This product may have been removed or the link is incorrect.'}
        action={
          <Button variant="outline" icon={ArrowLeft} onClick={() => navigate('/settings/products')}>
            Back to Products
          </Button>
        }
      />
    )
  }

  const { product, grades, prices } = data
  const gradeName = (gradeId: number) =>
    grades.find((g) => g.id === gradeId)?.name ?? `Grade #${gradeId}`

  const gradeColumns: Column<GradeDefinition>[] = [
    {
      header: 'Code',
      cell: (g) => <span className="font-mono text-xs text-muted-foreground">{g.gradeCode}</span>,
    },
    { header: 'Name', cell: (g) => <span className="font-medium text-foreground">{g.name}</span> },
    {
      header: 'Description',
      className: 'hidden md:table-cell',
      cell: (g) => <span className="text-muted-foreground">{g.description ?? '—'}</span>,
    },
    { header: 'Order', align: 'end', cell: (g) => g.displayOrder },
    { header: 'Status', cell: (g) => <ActiveBadge active={g.active} /> },
    {
      header: 'Actions',
      align: 'end',
      srOnlyHeader: true,
      cell: (g) => (
        <Button
          variant="ghost"
          size="sm"
          icon={Pencil}
          aria-label={`Edit ${g.name}`}
          onClick={() => setGradeModal({ open: true, grade: g })}
        >
          Edit
        </Button>
      ),
    },
  ]

  const priceColumns: Column<PriceRule>[] = [
    { header: 'Grade', cell: (p) => <span className="font-medium text-foreground">{gradeName(p.gradeId)}</span> },
    {
      header: 'Amount',
      align: 'end',
      cell: (p) => <span className="font-medium">{formatMoney(p.amount, p.currency)}</span>,
    },
    { header: 'Currency', cell: (p) => p.currency },
    {
      header: 'Effective From',
      className: 'hidden sm:table-cell',
      cell: (p) => formatDate(p.effectiveFrom),
    },
    {
      header: 'Effective To',
      className: 'hidden md:table-cell',
      cell: (p) => (p.effectiveTo ? formatDate(p.effectiveTo) : '—'),
    },
    { header: 'Status', cell: (p) => <ActiveBadge active={p.active} /> },
    {
      header: 'Actions',
      align: 'end',
      srOnlyHeader: true,
      cell: (p) => (
        <Button
          variant="ghost"
          size="sm"
          icon={Pencil}
          aria-label={`Edit price for ${gradeName(p.gradeId)}`}
          onClick={() => setPriceModal({ open: true, price: p })}
        >
          Edit
        </Button>
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
        <Link to="/settings/products" className="hover:text-foreground hover:underline">
          Products
        </Link>
        <span aria-hidden="true">/</span>
        <span className="text-foreground">{product.name}</span>
      </div>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="font-heading text-xl font-semibold text-foreground sm:text-2xl">
              {product.name}
            </h1>
            <ActiveBadge active={product.active} />
          </div>
          <p className="mt-0.5 text-sm text-muted-foreground">
            {product.variety} · {product.unit}
          </p>
        </div>
        <Button variant="outline" icon={ArrowLeft} onClick={() => navigate('/settings/products')}>
          Back
        </Button>
      </div>

      <Card flush>
        <div className="flex items-center justify-between border-b border-border px-4">
          <div role="tablist" aria-label="Product settings" className="flex">
            <button
              role="tab"
              aria-selected={tab === 'grades'}
              onClick={() => setTab('grades')}
              className={cn(
                '-mb-px flex items-center gap-2 border-b-2 px-4 py-3 text-sm font-medium transition-colors cursor-pointer',
                tab === 'grades'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:text-foreground',
              )}
            >
              <Star className="h-4 w-4" aria-hidden="true" />
              Grades
            </button>
            <button
              role="tab"
              aria-selected={tab === 'pricing'}
              onClick={() => setTab('pricing')}
              className={cn(
                '-mb-px flex items-center gap-2 border-b-2 px-4 py-3 text-sm font-medium transition-colors cursor-pointer',
                tab === 'pricing'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:text-foreground',
              )}
            >
              <DollarSign className="h-4 w-4" aria-hidden="true" />
              Pricing
            </button>
          </div>
          {tab === 'grades' ? (
            <Button size="sm" icon={Plus} onClick={() => setGradeModal({ open: true, grade: null })}>
              Add Grade
            </Button>
          ) : (
            <Button
              size="sm"
              icon={Plus}
              disabled={grades.length === 0}
              onClick={() => setPriceModal({ open: true, price: null })}
            >
              Add Price
            </Button>
          )}
        </div>

        {tab === 'grades' ? (
          <Table
            columns={gradeColumns}
            rows={grades}
            rowKey={(g) => String(g.id)}
            empty={
              <EmptyState
                icon={Star}
                title="No grades yet"
                description="Define quality grades (e.g. Grade A, B, C) for this product."
                action={
                  <Button icon={Plus} onClick={() => setGradeModal({ open: true, grade: null })}>
                    Add Grade
                  </Button>
                }
              />
            }
          />
        ) : grades.length === 0 ? (
          <div className="p-4">
            <Alert variant="info" title="Add grades first">
              Pricing is set per grade. Add at least one grade before configuring prices.
            </Alert>
          </div>
        ) : (
          <Table
            columns={priceColumns}
            rows={prices}
            rowKey={(p) => String(p.id)}
            empty={
              <EmptyState
                icon={DollarSign}
                title="No prices yet"
                description="Set grade-based prices in USD or LBP."
                action={
                  <Button icon={Plus} onClick={() => setPriceModal({ open: true, price: null })}>
                    Add Price
                  </Button>
                }
              />
            }
          />
        )}
      </Card>

      <GradeFormModal
        open={gradeModal.open}
        grade={gradeModal.grade}
        productId={productId}
        onClose={() => setGradeModal({ open: false, grade: null })}
        onSaved={refresh}
      />
      <PriceFormModal
        open={priceModal.open}
        price={priceModal.price}
        productId={productId}
        grades={grades}
        onClose={() => setPriceModal({ open: false, price: null })}
        onSaved={refresh}
      />
    </div>
  )
}
