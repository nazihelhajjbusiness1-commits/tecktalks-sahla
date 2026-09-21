import type {
  DashboardStats,
  Delivery,
  InventorySummaryRow,
  PaymentsBreakdown,
} from '@/types'
import { mockDelay } from './api'
import { deliveries, farmers, inventory, payments } from './mockData'

/** "Today" for the mock dataset (matches the seeded delivery dates). */
const TODAY = '2026-09-11'

function computeStats(): DashboardStats {
  const todays = deliveries.filter((d) => d.date === TODAY)
  return {
    todaysDeliveries: todays.length,
    produceReceivedKg: todays.reduce((sum, d) => sum + d.netWeight, 0),
    activeFarmers: farmers.filter((f) => f.status === 'ACTIVE').length,
    paymentsDueUsd: payments.reduce((sum, p) => sum + p.remaining, 0),
    inventoryKg: inventory.reduce((sum, i) => sum + i.quantity, 0),
  }
}

function computeInventorySummary(): InventorySummaryRow[] {
  return inventory
    .filter((i) => i.quantity > 0)
    .sort((a, b) => b.quantity - a.quantity)
    .slice(0, 5)
    .map((i) => ({
      label: `${i.product} · Grade ${i.grade}`,
      quantityKg: i.quantity,
    }))
}

function computePaymentsBreakdown(): PaymentsBreakdown {
  return {
    paid: payments.filter((p) => p.status === 'paid').length,
    partial: payments.filter((p) => p.status === 'partial').length,
    pending: payments.filter((p) => p.status === 'pending').length,
  }
}

export interface DashboardData {
  stats: DashboardStats
  recentDeliveries: Delivery[]
  inventorySummary: InventorySummaryRow[]
  paymentsBreakdown: PaymentsBreakdown
}

// The dashboard has no backend endpoint yet (future sprint), so it always
// serves mock data regardless of VITE_USE_MOCKS. Only the Sprint 2 modules
// (farmers, products, grades, pricing, auth) talk to the real backend.
export const dashboardService = {
  async load(): Promise<DashboardData> {
    return mockDelay({
      stats: computeStats(),
      recentDeliveries: [...deliveries]
        .sort((a, b) => b.date.localeCompare(a.date))
        .slice(0, 6),
      inventorySummary: computeInventorySummary(),
      paymentsBreakdown: computePaymentsBreakdown(),
    })
  },
}
