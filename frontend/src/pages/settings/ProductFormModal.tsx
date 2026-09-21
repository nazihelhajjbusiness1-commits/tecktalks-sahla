import { useEffect, useState } from 'react'
import { Modal, Input, Select, Button } from '@/components/common'
import { productService } from '@/services/productService'
import type { Product, ProductUnit } from '@/types'

interface ProductFormModalProps {
  open: boolean
  onClose: () => void
  onSaved: (product: Product) => void
  product?: Product | null
}

interface FormState {
  name: string
  variety: string
  unit: ProductUnit
  active: boolean
}

type FieldErrors = Partial<Record<'name' | 'variety', string>>

const FORM_ID = 'product-form'
const UNITS: ProductUnit[] = ['KG', 'TON', 'BOX', 'CRATE']
const emptyForm: FormState = { name: '', variety: '', unit: 'KG', active: true }

function validate(values: FormState): FieldErrors {
  const errors: FieldErrors = {}
  if (!values.name.trim()) errors.name = 'Product name is required'
  if (!values.variety.trim()) errors.variety = 'Variety is required'
  return errors
}

export function ProductFormModal({
  open,
  onClose,
  onSaved,
  product,
}: ProductFormModalProps) {
  const isEdit = Boolean(product)
  const [values, setValues] = useState<FormState>(emptyForm)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | undefined>()
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!open) return
    setValues(
      product
        ? {
            name: product.name,
            variety: product.variety,
            unit: product.unit,
            active: product.active,
          }
        : emptyForm,
    )
    setErrors({})
    setFormError(undefined)
    setSubmitting(false)
  }, [open, product])

  const setField = <K extends keyof FormState>(field: K, value: FormState[K]) =>
    setValues((v) => ({ ...v, [field]: value }))

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setFormError(undefined)
    const nextErrors = validate(values)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    try {
      const saved =
        isEdit && product
          ? await productService.update(product.id, {
              name: values.name.trim(),
              variety: values.variety.trim(),
              unit: values.unit,
              active: values.active,
            })
          : await productService.create({
              name: values.name.trim(),
              variety: values.variety.trim(),
              unit: values.unit,
            })
      onSaved(saved)
      onClose()
    } catch (err) {
      setFormError(
        err instanceof Error ? err.message : 'Something went wrong. Please try again.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit Product' : 'Add Product'}
      description={
        isEdit
          ? 'Update this product’s details.'
          : 'Add a product to the catalog.'
      }
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button type="submit" form={FORM_ID} loading={submitting}>
            {isEdit ? 'Save Changes' : 'Save Product'}
          </Button>
        </>
      }
    >
      <form id={FORM_ID} onSubmit={handleSubmit} noValidate className="space-y-4">
        {formError && (
          <div
            role="alert"
            className="rounded-md border border-danger/30 bg-danger-soft px-4 py-3 text-sm text-foreground"
          >
            {formError}
          </div>
        )}

        <Input
          label="Name"
          required
          value={values.name}
          onChange={(e) => setField('name', e.target.value)}
          onBlur={() => setErrors((p) => ({ ...p, name: validate(values).name }))}
          error={errors.name}
          placeholder="e.g. Apple"
          autoComplete="off"
        />

        <Input
          label="Variety"
          required
          value={values.variety}
          onChange={(e) => setField('variety', e.target.value)}
          onBlur={() =>
            setErrors((p) => ({ ...p, variety: validate(values).variety }))
          }
          error={errors.variety}
          placeholder="e.g. Lebanese Red"
          autoComplete="off"
        />

        <Select
          label="Unit"
          value={values.unit}
          onChange={(e) => setField('unit', e.target.value as ProductUnit)}
          options={UNITS.map((u) => ({ label: u, value: u }))}
        />

        {isEdit && (
          <Select
            label="Status"
            value={values.active ? 'true' : 'false'}
            onChange={(e) => setField('active', e.target.value === 'true')}
            options={[
              { label: 'Active', value: 'true' },
              { label: 'Inactive', value: 'false' },
            ]}
          />
        )}
      </form>
    </Modal>
  )
}
