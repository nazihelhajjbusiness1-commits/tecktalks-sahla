import type { Payment } from '@/types'
import { mockDelay } from './api'
import { payments } from './mockData'

// Payments have no backend endpoint yet (future sprint) — always mock data.
export const paymentService = {
  async list(): Promise<Payment[]> {
    return mockDelay(payments)
  },
}
