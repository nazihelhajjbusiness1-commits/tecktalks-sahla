import type { Delivery } from '@/types'
import { USE_MOCKS, mockDelay, request } from './api'
import { deliveries } from './mockData'

export const deliveryService = {
  async list(): Promise<Delivery[]> {
    if (USE_MOCKS) return mockDelay(deliveries)
    return request<Delivery[]>('/deliveries')
  },

  async listByFarmer(farmerId: string): Promise<Delivery[]> {
    if (USE_MOCKS)
      return mockDelay(deliveries.filter((d) => d.farmerId === farmerId))
    return request<Delivery[]>(`/deliveries?farmerId=${farmerId}`)
  },
}
