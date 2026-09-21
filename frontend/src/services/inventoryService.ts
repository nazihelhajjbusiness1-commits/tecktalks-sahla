import type { InventoryItem } from '@/types'
import { mockDelay } from './api'
import { inventory } from './mockData'

// Inventory has no backend endpoint yet (future sprint) — always mock data.
export const inventoryService = {
  async list(): Promise<InventoryItem[]> {
    return mockDelay(inventory)
  },
}
