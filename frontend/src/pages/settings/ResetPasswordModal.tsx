import { useState } from 'react'
import { Modal, Button, Input, Alert } from '@/components/common'
import { userService } from '@/services/userService'
import { ApiError } from '@/services/api'
import type { StaffUser } from '@/types'

interface ResetPasswordModalProps {
  /** The user whose password is being reset; null closes the modal. */
  user: StaffUser | null
  onClose: () => void
  onDone: () => void
}

export function ResetPasswordModal({ user, onClose, onDone }: ResetPasswordModalProps) {
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit() {
    setError(null)
    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    if (!user) return
    setSubmitting(true)
    try {
      await userService.resetPassword(user.id, password)
      onDone()
    } catch (err) {
      setError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'Could not reset the password.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={Boolean(user)}
      onClose={() => (submitting ? undefined : onClose())}
      title="Reset password"
      description={
        user
          ? `Set a new password for ${user.firstname} ${user.lastname}.`
          : undefined
      }
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button loading={submitting} onClick={submit}>
            Reset password
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        {error && (
          <Alert variant="danger" onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}
        <Input
          label="New password"
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          hint="At least 8 characters. Share it securely with the user."
        />
      </div>
    </Modal>
  )
}
