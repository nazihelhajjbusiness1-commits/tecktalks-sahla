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
import { formatDate } from '@/utils/format'
import type { StaffUser } from '@/types'
import { CreateUserModal } from './CreateUserModal'

export function UsersSettingsPage() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'admin'
  const [modalOpen, setModalOpen] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)

  const { data, loading, error } = useAsync(
    () => (isAdmin ? userService.list() : Promise.resolve<StaffUser[]>([])),
    [isAdmin, reloadKey],
  )

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
    { header: 'Username', cell: (u) => u.username },
    { header: 'Email', cell: (u) => u.email },
    { header: 'Role', cell: (u) => roleLabels[u.role] ?? u.role },
    { header: 'Created', align: 'end', cell: (u) => formatDate(u.createdAt) },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Users"
        description="Manage staff accounts and access"
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
          setReloadKey((k) => k + 1)
        }}
      />
    </div>
  )
}
