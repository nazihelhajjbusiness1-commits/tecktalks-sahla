import { useEffect, useState } from 'react'
import { Modal, Input, Select, Button } from '@/components/common'
import { pricingService } from '@/services/pricingService'
import type { Currency, GradeDefinition, PriceRule } from '@/types'

interface PriceFormModalProps {
  open: boolean
  onClose: () => void
  onSaved: (price: PriceRule) => void
  productId: number
  grades: GradeDefinition[]
  price?: PriceRule | null
}

interface FormState {
  gradeId: string
  amount: string
  currency: Currency
  effectiveFrom: string
  effectiveTo: string
  active: boolean
}

type FieldErrors = Partial<
  Record<'gradeId' | 'amount' | 'effectiveFrom', string>
>

const FORM_ID = 'price-form'

function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10)
}

function emptyForm(): FormState {
  return {
    gradeId: '',
    amount: '',
    currency: 'USD',
    effectiveFrom: todayIsoDate(),
    effectiveTo: '',
    active: true,
  }
}

/** date input (YYYY-MM-DD) -> ISO date-time the backend accepts. */
function toIso(date: string): string {
  return `${date}T00:00:00Z`
}

function validate(values: FormState, isEdit: boolean): FieldErrors {
  const errors: FieldErrors = {}
  if (!isEdit && !values.gradeId) errors.gradeId = 'Select a grade'
  const amount = Number(values.amount)
  if (values.amount.trim() === '' || Number.isNaN(amount) || amount < 0)
    errors.amount = 'Enter an amount of zero or greater'
  if (!values.effectiveFrom) errors.effectiveFrom = 'Effective from is required'
  return errors
}

export function PriceFormModal({
  open,
  onClose,
  onSaved,
  productId,
  grades,
  price,
}: PriceFormModalProps) {
  const isEdit = Boolean(price)
  const [values, setValues] = useState<FormState>(emptyForm)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | undefined>()
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!open) return
    setValues(
      price
        ? {
            gradeId: String(price.gradeId),
            amount: String(price.amount),
            currency: price.currency,
            effectiveFrom: price.effectiveFrom.slice(0, 10),
            effectiveTo: price.effectiveTo ? price.effectiveTo.slice(0, 10) : '',
            active: price.active,
          }
        : emptyForm(),
    )
    setErrors({})
    setFormError(undefined)
    setSubmitting(false)
  }, [open, price])

  const setField = <K extends keyof FormState>(field: K, value: FormState[K]) =>
    setValues((v) => ({ ...v, [field]: value }))

  const gradeName = (id: number) =>
    grades.find((g) => g.id === id)?.name ?? `Grade #${id}`

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setFormError(undefined)
    const nextErrors = validate(values, isEdit)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    try {
      const amount = Number(values.amount)
      const effectiveFrom = toIso(values.effectiveFrom)
      const effectiveTo = values.effectiveTo ? toIso(values.effectiveTo) : null
      const saved =
        isEdit && price
          ? await pricingService.update(price.id, {
              amount,
              currency: values.currency,
              effectiveFrom,
              effectiveTo,
              active: values.active,
            })
          : await pricingService.create(productId, {
              gradeId: Number(values.gradeId),
              amount,
              currency: values.currency,
              effectiveFrom,
              effectiveTo,
              active: true,
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
      title={isEdit ? 'Edit Price' : 'Add Price'}
      description={
        isEdit
          ? `Update the ${gradeName(price!.gradeId)} price.`
          : 'Set a grade-based price for this product.'
      }
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button type="submit" form={FORM_ID} loading={submitting}>
            {isEdit ? 'Save Changes' : 'Save Price'}
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

        <Select
          label="Grade"
          required
          disabled={isEdit}
          placeholder="Select a grade"
          value={values.gradeId}
          onChange={(e) => setField('gradeId', e.target.value)}
          error={errors.gradeId}
          options={grades.map((g) => ({
            label: `${g.name} (${g.gradeCode})`,
            value: String(g.id),
          }))}
          hint={isEdit ? 'Grade cannot be changed for an existing price.' : undefined}
        />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            label="Amount"
            required
            type="number"
            min={0}
            step="0.01"
            value={values.amount}
            onChange={(e) => setField('amount', e.target.value)}
            onBlur={() =>
              setErrors((p) => ({ ...p, amount: validate(values, isEdit).amount }))
            }
            error={errors.amount}
            placeholder="e.g. 1.20"
          />
          <Select
            label="Currency"
            value={values.currency}
            onChange={(e) => setField('currency', e.target.value as Currency)}
            options={[
              { label: 'USD ($)', value: 'USD' },
              { label: 'LBP (ل.ل)', value: 'LBP' },
            ]}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            label="Effective From"
            required
            type="date"
            value={values.effectiveFrom}
            onChange={(e) => setField('effectiveFrom', e.target.value)}
            error={errors.effectiveFrom}
          />
          <Input
            label="Effective To"
            type="date"
            value={values.effectiveTo}
            onChange={(e) => setField('effectiveTo', e.target.value)}
            hint="Optional — leave blank for open-ended."
          />
        </div>

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
