import { useNavigate } from 'react-router-dom'
import { Compass } from 'lucide-react'
import { Button, EmptyState } from '@/components/common'

export function NotFoundPage() {
  const navigate = useNavigate()
  return (
    <div className="flex min-h-dvh items-center justify-center bg-background px-4">
      <EmptyState
        icon={Compass}
        title="Page not found"
        description="The page you're looking for doesn't exist or has moved."
        action={
          <Button onClick={() => navigate('/dashboard')}>Back to Dashboard</Button>
        }
      />
    </div>
  )
}
