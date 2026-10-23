import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate, type Location } from 'react-router-dom'
import { Mail, Lock, Eye, EyeOff, Sprout } from 'lucide-react'
import { Button, Input, Alert } from '@/components/common'
import { Logo } from '@/components/layout/Logo'
import { useAuth } from '@/hooks/useAuth'
import { brand } from '@/config/brand'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from =
    (location.state as { from?: Location } | null)?.from?.pathname ??
    '/dashboard'

  const [identifier, setIdentifier] = useState('rania.aoun@sahla.app')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login({ identifier, password })
      navigate(from, { replace: true })
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Something went wrong. Please try again.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid min-h-dvh lg:grid-cols-2">
      {/* Brand panel — the full Sahla logo over the context copy; hidden on small screens */}
      <aside className="hidden overflow-hidden bg-white p-10 lg:flex lg:flex-col lg:justify-center xl:p-16">
        <Logo full className="w-full max-w-md" />
        <div className="mt-10 max-w-md">
          <h2 className="font-heading text-3xl font-semibold leading-tight text-foreground">
            From delivery to payment, one place to run your collection center.
          </h2>
          <p className="mt-4 text-muted-foreground">{brand.description}</p>
          <ul className="mt-8 space-y-3 text-sm text-foreground/80">
            {[
              'Track every farmer delivery and grade',
              'Keep inventory and payments in sync',
              'Built for cooperatives across Lebanon',
            ].map((line) => (
              <li key={line} className="flex items-center gap-2.5">
                <Sprout className="h-4.5 w-4.5 text-primary" aria-hidden="true" />
                {line}
              </li>
            ))}
          </ul>
        </div>
      </aside>

      {/* Form panel — brand-green backdrop with the form on a white card */}
      <main className="flex items-center justify-center bg-primary px-4 py-10 sm:px-8">
        <div className="w-full max-w-sm rounded-2xl bg-card p-6 shadow-raised sm:p-8">
          <div className="mb-8 lg:hidden">
            <Logo full className="h-16" />
          </div>

          <div className="mb-6">
            <h1 className="font-heading text-2xl font-semibold text-foreground">
              Welcome back
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Sign in to your {brand.name} account to continue.
            </p>
          </div>

          {error && (
            <Alert variant="danger" className="mb-4" title="Sign in failed">
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <Input
              label="Email or username"
              type="text"
              autoComplete="username"
              icon={Mail}
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              placeholder="you@cooperative.lb"
              required
            />

            <Input
              label="Password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              icon={Lock}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
              trailing={
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  aria-pressed={showPassword}
                  className="flex h-8 w-8 items-center justify-center rounded text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer"
                >
                  {showPassword ? (
                    <EyeOff className="h-4.5 w-4.5" aria-hidden="true" />
                  ) : (
                    <Eye className="h-4.5 w-4.5" aria-hidden="true" />
                  )}
                </button>
              }
            />

            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 text-sm text-muted-foreground">
                <input
                  type="checkbox"
                  className="h-4 w-4 rounded border-input text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring accent-primary"
                />
                Remember me
              </label>
              <button
                type="button"
                className="text-sm font-medium text-primary hover:underline cursor-pointer"
              >
                Forgot password?
              </button>
            </div>

            <Button type="submit" block size="lg" loading={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>

          <p className="mt-6 rounded-md bg-muted px-3 py-2.5 text-xs text-muted-foreground">
            <span className="font-medium text-foreground">Demo:</span> any email
            + password signs in. Use{' '}
            <span className="font-medium text-foreground">wrong@demo</span> to
            preview the error state.
          </p>
        </div>
      </main>
    </div>
  )
}
