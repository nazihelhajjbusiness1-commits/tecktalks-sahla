import { useEffect, useState } from 'react'
import { Modal, Input, Select, Button } from '@/components/common'
import { gradeService } from '@/services/gradeService'
import { ApiError } from '@/services/api'
import type { GradeDefinition } from '@/types'

interface GradeFormModalProps {
  open: boolean
  onClose: () => void
  onSaved: (grade: GradeDefinition) => void
  productId: number
  grade?: GradeDefinition | null
}

interface FormState {
  gradeCode: string
  name: string
  description: string
  displayOrder: string
  active: boolean
}

type FieldErrors = Partial<
  Record<'gradeCode' | 'name' | 'displayOrder', string>
>

const FORM_ID = 'grade-form'
const emptyForm: FormState = {
  gradeCode: '',
  name: '',
  description: '',
  displayOrder: '0',
  active: true,
}

function validate(values: FormState, isEdit: boolean): FieldErrors {
  const errors: FieldErrors = {}
  if (!isEdit && !values.gradeCode.trim())
    errors.gradeCode = 'Grade code is required'
  if (!values.name.trim()) errors.name = 'Name is required'
  const order = Number(values.displayOrder)
  if (values.displayOrder.trim() === '' || Number.isNaN(order) || order < 0)
    errors.displayOrder = 'Enter a non-negative number'
  return errors
}

export function GradeFormModal({
  open,
  onClose,
  onSaved,
  productId,
  grade,
}: GradeFormModalProps) {
  const isEdit = Boolean(grade)
  const [values, setValues] = useState<FormState>(emptyForm)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [codeError, setCodeError] = useState<string | undefined>()
  const [formError, setFormError] = useState<string | undefined>()
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!open) return
    setValues(
      grade
        ? {
            gradeCode: grade.gradeCode,
            name: grade.name,
            description: grade.description ?? '',
            displayOrder: String(grade.displayOrder),
            active: grade.active,
          }
        : emptyForm,
    )
    setErrors({})
    setCodeError(undefined)
    setFormError(undefined)
    setSubmitting(false)
  }, [open, grade])

  const setField = <K extends keyof FormState>(field: K, value: FormState[K]) =>
    setValues((v) => ({ ...v, [field]: value }))

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setFormError(undefined)
    setCodeError(undefined)
    const nextErrors = validate(values, isEdit)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    try {
      const displayOrder = Number(values.displayOrder)
      const description = values.description.trim() || undefined
      const saved =
        isEdit && grade
          ? await gradeService.update(grade.id, {
              name: values.name.trim(),
              description,
              displayOrder,
              active: values.active,
            })
          : await gradeService.create(productId, {
              gradeCode: values.gradeCode.trim(),
              name: values.name.trim(),
              description,
              displayOrder,
              active: true,
            })
      onSaved(saved)
      onClose()
    } catch (err) {
      if (err instanceof ApiError && err.status === 409 && !isEdit) {
        setCodeError(err.message)
      } else {
        setFormError(
          err instanceof Error ? err.message : 'Something went wrong. Please try again.',
        )
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit Grade' : 'Add Grade'}
      description={
        isEdit
          ? 'Update this grade definition.'
          : 'Define a quality grade for this product.'
      }
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button type="submit" form={FORM_ID} loading={submitting}>
            {isEdit ? 'Save Changes' : 'Save Grade'}
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

        {!isEdit && (
          <Input
            label="Grade Code"
            required
            value={values.gradeCode}
            onChange={(e) => setField('gradeCode', e.target.value)}
            onBlur={() =>
              setErrors((p) => ({
                ...p,
                gradeCode: validate(values, isEdit).gradeCode,
              }))
            }
            error={codeError ?? errors.gradeCode}
            placeholder="e.g. GRADE_A"
            autoComplete="off"
          />
        )}

        <Input
          label="Name"
          required
          value={values.name}
          onChange={(e) => setField('name', e.target.value)}
          onBlur={() =>
            setErrors((p) => ({ ...p, name: validate(values, isEdit).name }))
          }
          error={errors.name}
          placeholder="e.g. Grade A"
          autoComplete="off"
        />

        <Input
          label="Description"
          value={values.description}
          onChange={(e) => setField('description', e.target.value)}
          hint="Optional short note about this grade."
          autoComplete="off"
        />

        <Input
          label="Display Order"
          required
          type="number"
          min={0}
          value={values.displayOrder}
          onChange={(e) => setField('displayOrder', e.target.value)}
          onBlur={() =>
            setErrors((p) => ({
              ...p,
              displayOrder: validate(values, isEdit).displayOrder,
            }))
          }
          error={errors.displayOrder}
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
