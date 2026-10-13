import { useState } from 'react'
import { Modal, Button, Input, Select, Alert } from '@/components/common'
import { userService, roleLabels } from '@/services/userService'
import { ApiError } from '@/services/api'
import type { BackendRole } from '@/types'

const roleOptions = (Object.keys(roleLabels) as BackendRole[]).map((value) => ({
  label: roleLabels[value],
  value,
}))

interface CreateUserModalProps {
  open: boolean
  onClose: () => void
  onCreated: () => void
}

export function CreateUserModal({ open, onClose, onCreated }: CreateUserModalProps) {
  const [firstname, setFirstname] = useState('')
  const [lastname, setLastname] = useState('')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<BackendRole>('RECEIVING_EMPLOYEE')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function reset() {
    setFirstname('')
    setLastname('')
    setUsername('')
    setEmail('')
    setPhoneNumber('')
    setPassword('')
    setRole('RECEIVING_EMPLOYEE')
    setError(null)
  }

  async function submit() {
    setError(null)
    if (!firstname.trim() || !lastname.trim() || !username.trim() || !email.trim()) {
      setError('First name, last name, username and email are required.')
      return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    setSubmitting(true)
    try {
      await userService.create({
        firstname: firstname.trim(),
        lastname: lastname.trim(),
        username: username.trim(),
        email: email.trim(),
        phoneNumber: phoneNumber.trim() || undefined,
        password,
        role,
      })
      reset()
      onCreated()
    } catch (err) {
      setError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'Could not create the user.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={() => (submitting ? undefined : onClose())}
      title="Create user account"
      description="The new account can sign in immediately with the password you set."
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button loading={submitting} onClick={submit}>
            Create account
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

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            label="First name"
            required
            value={firstname}
            onChange={(e) => setFirstname(e.target.value)}
          />
          <Input
            label="Last name"
            required
            value={lastname}
            onChange={(e) => setLastname(e.target.value)}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            label="Username"
            required
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <Input
            label="Email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            label="Phone (optional)"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
          <Select
            label="Role"
            value={role}
            options={roleOptions}
            onChange={(e) => setRole(e.target.value as BackendRole)}
          />
        </div>

        <Input
          label="Temporary password"
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          hint="At least 8 characters. Share it securely with the new user."
        />
      </div>
    </Modal>
  )
}
