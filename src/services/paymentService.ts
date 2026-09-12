import type { Payment } from '@/types'
import { USE_MOCKS, mockDelay, request } from './api'
import { payments } from './mockData'

export const paymentService = {
  async list(): Promise<Payment[]> {
    if (USE_MOCKS) return mockDelay(payments)
    return request<Payment[]>('/payments')
  },
}
