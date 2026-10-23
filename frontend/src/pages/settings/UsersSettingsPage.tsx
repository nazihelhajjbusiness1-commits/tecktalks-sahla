import { useState } from 'react'
import { UserPlus, Users } from 'lucide-react'
import {
  PageHeader,
  Card,
  Button,
  Table,
  Alert,
  EmptyState,
  type Column,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/hooks/useAuth'
import { userService, roleLabels } from '@/services/userService'
import { ApiError } from '@/services/api'
import { formatDate } from '@/utils/format'
import { cn } from '@/utils/cn'
import type { BackendRole, StaffUser } from '@/types'
import { CreateUserModal } from './CreateUserModal'
import { ResetPasswordModal } from './ResetPasswordModal'

const roleOptions = (Object.keys(roleLabels) as BackendRole[]).map((value) => ({
  label: roleLabels[value],
  value,
}))

export function UsersSettingsPage() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'admin'
  const [modalOpen, setModalOpen] = useState(false)
  const [resetUser, setResetUser] = useState<StaffUser | null>(null)
  const [reloadKey, setReloadKey] = useState(0)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const { data, loading, error } = useAsync(
    () => (isAdmin ? userService.list() : Promise.resolve<StaffUser[]>([])),
    [isAdmin, reloadKey],
  )

  const reload = () => setReloadKey((k) => k + 1)

  async function runAction(id: number, action: () => Promise<unknown>) {
    setActionError(null)
    setBusyId(id)
    try {
      await action()
      reload()
    } catch (err) {
      setActionError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'The change could not be saved.',
      )
    } finally {
      setBusyId(null)
    }
  }

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <PageHeader title="Users" description="Manage staff accounts and access" />
        <Alert variant="warning" title="Administrators only">
          You need an administrator account to manage users.
        </Alert>
      </div>
    )
  }

  const columns: Column<StaffUser>[] = [
    {
      header: 'Name',
      cell: (u) => (
        <span className="font-medium">
          {u.firstname} {u.lastname}
        </span>
      ),
    },
    { header: 'Email', cell: (u) => u.email },
    {
      header: 'Role',
      cell: (u) => {
        const isSelf = u.email === user?.email
        return (
          <select
            aria-label={`Role for ${u.firstname} ${u.lastname}`}
            value={u.role}
            disabled={busyId === u.id || isSelf}
            onChange={(e) =>
              runAction(u.id, () =>
                userService.update(u.id, { role: e.target.value as BackendRole }),
              )
            }
            className="rounded-md border border-input bg-background px-2 py-1 text-sm text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:opacity-60"
          >
            {roleOptions.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
        )
      },
    },
    {
      header: 'Status',
      cell: (u) => (
        <span
          className={cn(
            'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
            u.status === 'ACTIVE'
              ? 'bg-success-soft text-success'
              : 'bg-muted text-muted-foreground',
          )}
        >
          {u.status === 'ACTIVE' ? 'Active' : 'Disabled'}
        </span>
      ),
    },
    { header: 'Created', cell: (u) => formatDate(u.createdAt) },
    {
      header: 'Actions',
      align: 'end',
      cell: (u) => {
        const isSelf = u.email === user?.email
        const busy = busyId === u.id
        return (
          <div className="flex items-center justify-end gap-2">
            <Button
              variant={u.status === 'ACTIVE' ? 'danger' : 'outline'}
              size="sm"
              loading={busy}
              disabled={isSelf}
              onClick={() =>
                runAction(u.id, () =>
                  userService.update(u.id, {
                    status: u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE',
                  }),
                )
              }
            >
              {u.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
            </Button>
            <Button
              variant="ghost"
              size="sm"
              disabled={busy}
              onClick={() => setResetUser(u)}
            >
              Reset password
            </Button>
          </div>
        )
      },
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Users"
        description="Manage staff accounts, roles, and access"
        actions={
          <Button icon={UserPlus} onClick={() => setModalOpen(true)}>
            Add user
          </Button>
        }
      />

      {error && (
        <Alert variant="danger" title="Couldn't load users">
          {error}
        </Alert>
      )}

      {actionError && (
        <Alert variant="danger" onDismiss={() => setActionError(null)}>
          {actionError}
        </Alert>
      )}

      <Card flush>
        <Table
          columns={columns}
          rows={data ?? []}
          rowKey={(u) => String(u.id)}
          loading={loading}
          empty={
            <EmptyState
              icon={Users}
              title="No users yet"
              description="Create the first staff account to get started."
            />
          }
        />
      </Card>

      <CreateUserModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onCreated={() => {
          setModalOpen(false)
          reload()
        }}
      />

      <ResetPasswordModal
        key={resetUser?.id ?? 'none'}
        user={resetUser}
        onClose={() => setResetUser(null)}
        onDone={() => setResetUser(null)}
      />
    </div>
  )
}
