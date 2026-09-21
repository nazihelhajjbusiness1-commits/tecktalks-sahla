import { useEffect, useState } from 'react'
import { Modal, Input, Select, Button } from '@/components/common'
import { farmerService } from '@/services/farmerService'
import { ApiError } from '@/services/api'
import type { Farmer, FarmerStatus } from '@/types'

interface FarmerFormModalProps {
  open: boolean
  onClose: () => void
  /** Called with the created/updated farmer after a successful save. */
  onSaved: (farmer: Farmer) => void
  /** When provided, the modal edits this farmer; otherwise it creates one. */
  farmer?: Farmer | null
}

interface FormState {
  farmerCode: string
  name: string
  phone: string
  village: string
  status: FarmerStatus
}

type FieldErrors = Partial<Record<'name' | 'phone' | 'village', string>>

const FORM_ID = 'farmer-form'

const emptyForm: FormState = {
  farmerCode: '',
  name: '',
  phone: '',
  village: '',
  status: 'ACTIVE',
}

// Lenient Lebanese-friendly phone check: digits with optional +, spaces, dashes.
const PHONE_RE = /^\+?[\d\s-]{6,}$/

function validate(values: FormState): FieldErrors {
  const errors: FieldErrors = {}
  if (!values.name.trim()) errors.name = 'Farmer name is required'
  if (!values.phone.trim()) errors.phone = 'Phone number is required'
  else if (!PHONE_RE.test(values.phone.trim()))
    errors.phone = 'Enter a valid phone number'
  if (!values.village.trim()) errors.village = 'Village is required'
  return errors
}

export function FarmerFormModal({
  open,
  onClose,
  onSaved,
  farmer,
}: FarmerFormModalProps) {
  const isEdit = Boolean(farmer)
  const [values, setValues] = useState<FormState>(emptyForm)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [codeError, setCodeError] = useState<string | undefined>()
  const [formError, setFormError] = useState<string | undefined>()
  const [submitting, setSubmitting] = useState(false)

  // Reset the form whenever the modal opens (or the target farmer changes).
  useEffect(() => {
    if (!open) return
    setValues(
      farmer
        ? {
            farmerCode: farmer.farmerCode,
            name: farmer.name,
            phone: farmer.phone,
            village: farmer.village,
            status: farmer.status,
          }
        : emptyForm,
    )
    setErrors({})
    setCodeError(undefined)
    setFormError(undefined)
    setSubmitting(false)
  }, [open, farmer])

  const setField = (field: keyof FormState, value: string) =>
    setValues((v) => ({ ...v, [field]: value }))

  const validateOnBlur = (field: keyof FieldErrors) =>
    setErrors((prev) => ({ ...prev, [field]: validate(values)[field] }))

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setFormError(undefined)
    setCodeError(undefined)

    const nextErrors = validate(values)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    try {
      const saved =
        isEdit && farmer
          ? await farmerService.update(farmer.id, {
              name: values.name.trim(),
              phone: values.phone.trim(),
              village: values.village.trim(),
              status: values.status,
            })
          : await farmerService.create({
              farmerCode: values.farmerCode.trim() || undefined,
              name: values.name.trim(),
              phone: values.phone.trim(),
              village: values.village.trim(),
            })
      onSaved(saved)
      onClose()
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        // Duplicate farmer code — surface it on the field when creating.
        if (!isEdit) setCodeError(err.message)
        else setFormError(err.message)
      } else if (err instanceof Error) {
        setFormError(err.message)
      } else {
        setFormError('Something went wrong. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit Farmer' : 'Add Farmer'}
      description={
        isEdit
          ? 'Update this farmer’s details.'
          : 'Register a new farmer with the cooperative.'
      }
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button type="submit" form={FORM_ID} loading={submitting}>
            {isEdit ? 'Save Changes' : 'Save Farmer'}
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
            label="Farmer Code"
            value={values.farmerCode}
            onChange={(e) => setField('farmerCode', e.target.value)}
            hint="Optional — a readable code is generated if left blank."
            error={codeError}
            placeholder="e.g. F-00042"
            autoComplete="off"
          />
        )}

        <Input
          label="Name"
          required
          value={values.name}
          onChange={(e) => setField('name', e.target.value)}
          onBlur={() => validateOnBlur('name')}
          error={errors.name}
          autoComplete="off"
        />

        <Input
          label="Phone"
          required
          type="tel"
          value={values.phone}
          onChange={(e) => setField('phone', e.target.value)}
          onBlur={() => validateOnBlur('phone')}
          error={errors.phone}
          placeholder="e.g. 70 123 456"
          autoComplete="off"
        />

        <Input
          label="Village"
          required
          value={values.village}
          onChange={(e) => setField('village', e.target.value)}
          onBlur={() => validateOnBlur('village')}
          error={errors.village}
          autoComplete="off"
        />

        {isEdit && (
          <Select
            label="Status"
            value={values.status}
            onChange={(e) => setField('status', e.target.value)}
            options={[
              { label: 'Active', value: 'ACTIVE' },
              { label: 'Inactive', value: 'INACTIVE' },
            ]}
          />
        )}
      </form>
    </Modal>
  )
}
