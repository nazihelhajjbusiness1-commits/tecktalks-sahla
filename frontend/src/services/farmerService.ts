import type { Farmer } from '@/types'
import { USE_MOCKS, mockDelay, request } from './api'
import { farmers } from './mockData'

export const farmerService = {
  async list(): Promise<Farmer[]> {
    if (USE_MOCKS) return mockDelay(farmers)
    return request<Farmer[]>('/farmers')
  },

  async getById(id: string): Promise<Farmer | undefined> {
    if (USE_MOCKS) return mockDelay(farmers.find((f) => f.id === id))
    return request<Farmer>(`/farmers/${id}`)
  },
}
