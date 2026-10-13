import type {
  FarmerSettlement,
  SettlementCalculation,
  SettlementCalculationRequest,
  SettlementPreview,
} from '@/types'
import { request } from './api'

/**
 * Settlement workflow API (Sprint 4, DT-74/75). The backend is the source of
 * truth for every monetary value — the UI only displays what these return.
 */
export const settlementService = {
  /** Current stored settlement for a delivery (404 if none yet). */
  getByDelivery(deliveryId: number): Promise<FarmerSettlement> {
    return request<FarmerSettlement>(`/deliveries/${deliveryId}/settlement`)
  },

  /** Resolve price + accepted weight before any calculation. */
  getPreview(deliveryId: number): Promise<SettlementPreview> {
    return request<SettlementPreview>(`/deliveries/${deliveryId}/settlement/preview`, {
      method: 'POST',
    })
  },

  /** Calculate gross/commission/deductions/net (persists a CALCULATED settlement). */
  calculate(
    deliveryId: number,
    payload: SettlementCalculationRequest,
  ): Promise<SettlementCalculation> {
    return request<SettlementCalculation>(
      `/deliveries/${deliveryId}/settlement/calculate`,
      { method: 'POST', json: payload },
    )
  },

  /** Freeze the calculation and post the farmer ledger credit. Idempotent. */
  confirm(deliveryId: number): Promise<FarmerSettlement> {
    return request<FarmerSettlement>(`/deliveries/${deliveryId}/settlement/confirm`, {
      method: 'POST',
    })
  },
}
