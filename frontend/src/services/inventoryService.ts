import type { InventoryItem } from '@/types'
import { mockDelay, request } from './api'
import { inventory } from './mockData'

export const inventoryService = {
  // The Inventory overview page still serves mock data (its own backend-backed
  // rebuild is a later task); the delivery details screen uses the real
  // movement-derived balance below.
  async list(): Promise<InventoryItem[]> {
    return mockDelay(inventory)
  },

  /**
   * DT-56/DT-61: current stock for a product, derived from the movement ledger.
   * Backend returns a bare numeric balance for `/inventory/balance/{productId}`.
   */
  balanceForProduct(productId: number): Promise<number> {
    return request<number>(`/inventory/balance/${productId}`)
  },
}
