import type { DashboardData } from '@/types'
import { USE_MOCKS, mockDelay, request } from './api'
import { deliveries, farmers, inventory, payments } from './mockData'

/** "Today" for the mock dataset (matches the seeded delivery dates). */
const TODAY = '2026-09-11'

/** Builds a dashboard payload from the local mock data (offline dev only). */
function mockDashboard(): DashboardData {
  const todays = deliveries.filter((d) => d.date === TODAY)
  return {
    todaysDeliveries: todays.length,
    produceReceivedTodayKg: todays.reduce((sum, d) => sum + d.netWeight, 0),
    activeFarmers: farmers.filter((f) => f.status === 'ACTIVE').length,
    currentInventoryKg: inventory.reduce((sum, i) => sum + i.quantity, 0),
    amountOwedToFarmers: [
      { currency: 'USD', amount: payments.reduce((sum, p) => sum + p.remaining, 0) },
    ],
    paymentsToday: [],
    recentDeliveries: [...deliveries]
      .sort((a, b) => b.date.localeCompare(a.date))
      .slice(0, 6)
      .map((d) => ({
        id: Number(d.id) || 0,
        farmerName: d.farmerName,
        productName: d.product,
        quantity: d.netWeight,
        unit: 'KG',
        status: d.status.toUpperCase(),
        deliveryDate: d.date,
      })),
    recentPayments: [],
  }
}

/**
 * DT-90: the dashboard now reads real aggregated statistics from the backend
 * (GET /api/dashboard). Mock mode is still honored for offline frontend work.
 */
export const dashboardService = {
  async load(): Promise<DashboardData> {
    if (USE_MOCKS) return mockDelay(mockDashboard())
    return request<DashboardData>('/dashboard')
  },
}
