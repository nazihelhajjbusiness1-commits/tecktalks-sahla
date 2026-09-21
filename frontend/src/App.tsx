import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from '@/hooks/useAuth'
import { ProtectedRoute } from '@/components/routing/ProtectedRoute'
import { AppLayout } from '@/components/layout/AppLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { FarmersPage } from '@/pages/farmers/FarmersPage'
import { FarmerDetailPage } from '@/pages/farmers/FarmerDetailPage'
import { DeliveriesPage } from '@/pages/deliveries/DeliveriesPage'
import { InventoryPage } from '@/pages/inventory/InventoryPage'
import { PaymentsPage } from '@/pages/payments/PaymentsPage'
import { ReportsPage } from '@/pages/reports/ReportsPage'
import { SettingsPage } from '@/pages/settings/SettingsPage'
import { ProductsSettingsPage } from '@/pages/settings/ProductsSettingsPage'
import { ProductSettingsDetailPage } from '@/pages/settings/ProductSettingsDetailPage'
import { NotFoundPage } from '@/pages/NotFoundPage'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* Authenticated area */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/farmers" element={<FarmersPage />} />
              <Route path="/farmers/:id" element={<FarmerDetailPage />} />
              <Route path="/deliveries" element={<DeliveriesPage />} />
              <Route path="/inventory" element={<InventoryPage />} />
              <Route path="/payments" element={<PaymentsPage />} />
              <Route path="/reports" element={<ReportsPage />} />
              <Route path="/settings" element={<SettingsPage />} />
              <Route path="/settings/products" element={<ProductsSettingsPage />} />
              <Route
                path="/settings/products/:id"
                element={<ProductSettingsDetailPage />}
              />
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
