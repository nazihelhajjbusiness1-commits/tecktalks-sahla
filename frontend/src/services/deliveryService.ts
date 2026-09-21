import type { Delivery } from '@/types'
import { mockDelay } from './api'
import { deliveries } from './mockData'

// Deliveries have no backend endpoint yet (future sprint) — always mock data.
export const deliveryService = {
  async list(): Promise<Delivery[]> {
    return mockDelay(deliveries)
  },

  async listByFarmer(farmerId: string): Promise<Delivery[]> {
    return mockDelay(deliveries.filter((d) => d.farmerId === farmerId))
  },
}
