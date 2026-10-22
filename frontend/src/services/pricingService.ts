import type {
  CreatePriceRequest,
  PriceRule,
  UpdatePriceRequest,
} from '@/types'
import { ApiError, USE_MOCKS, mockDelay, request } from './api'

/** In-memory mock price rules (fallback only). */
const mockPrices: PriceRule[] = [
  { id: 1, productId: 1, gradeId: 1, amount: 1.2, currency: 'USD', effectiveFrom: '2026-01-01T00:00:00Z', effectiveTo: null, active: true },
  { id: 2, productId: 1, gradeId: 2, amount: 0.8, currency: 'USD', effectiveFrom: '2026-01-01T00:00:00Z', effectiveTo: null, active: true },
  { id: 3, productId: 1, gradeId: 3, amount: 0.4, currency: 'USD', effectiveFrom: '2026-01-01T00:00:00Z', effectiveTo: null, active: true },
]

export const pricingService = {
  async listByProduct(productId: number): Promise<PriceRule[]> {
    if (USE_MOCKS) {
      return mockDelay(mockPrices.filter((p) => p.productId === productId))
    }
    return request<PriceRule[]>(`/products/${productId}/prices`)
  },

  async create(
    productId: number,
    body: CreatePriceRequest,
  ): Promise<PriceRule> {
    if (USE_MOCKS) {
      if (body.amount < 0) throw new ApiError(400, 'Price must be zero or greater')
      const created: PriceRule = {
        id: Math.max(0, ...mockPrices.map((p) => p.id)) + 1,
        productId,
        gradeId: body.gradeId,
        amount: body.amount,
        currency: body.currency,
        effectiveFrom: body.effectiveFrom,
        effectiveTo: body.effectiveTo ?? null,
        active: body.active ?? true,
      }
      mockPrices.push(created)
      return mockDelay(created)
    }
    return request<PriceRule>(`/products/${productId}/prices`, {
      method: 'POST',
      json: body,
    })
  },

  async update(priceId: number, body: UpdatePriceRequest): Promise<PriceRule> {
    if (USE_MOCKS) {
      const price = mockPrices.find((p) => p.id === priceId)
      if (!price) throw new ApiError(404, 'Price not found')
      if (body.amount < 0) throw new ApiError(400, 'Price must be zero or greater')
      price.amount = body.amount
      price.currency = body.currency
      price.effectiveFrom = body.effectiveFrom
      price.effectiveTo = body.effectiveTo ?? null
      price.active = body.active
      return mockDelay(price)
    }
    return request<PriceRule>(`/prices/${priceId}`, {
      method: 'PUT',
      json: body,
    })
  },
}
