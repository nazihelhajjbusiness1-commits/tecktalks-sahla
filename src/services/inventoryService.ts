import type { InventoryItem } from '@/types'
import { USE_MOCKS, mockDelay, request } from './api'
import { inventory } from './mockData'

export const inventoryService = {
  async list(): Promise<InventoryItem[]> {
    if (USE_MOCKS) return mockDelay(inventory)
    return request<InventoryItem[]>('/inventory')
  },
}
