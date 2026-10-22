import type {
  Currency,
  FarmerBalances,
  FarmerPayment,
  OutstandingFarmer,
  PagedResponse,
  PaymentMethod,
  RecordPaymentRequest,
  StatementLine,
} from '@/types'
import { buildQuery, request } from './api'

export interface StatementFilters {
  currency?: Currency
  from?: string
  to?: string
}

export interface PaymentHistoryFilters {
  currency?: Currency
  paymentMethod?: PaymentMethod
  from?: string
  to?: string
  page?: number
  size?: number
}

/**
 * Farmer payments + financial reads (Sprint 4, DT-72/73/76/77). Talks to the
 * real backend; all balances come from the ledger, never computed in the UI.
 */
export const paymentService = {
  /** Payment queue: farmers owed money, per-currency outstanding. */
  getOutstandingFarmers(): Promise<OutstandingFarmer[]> {
    return request<OutstandingFarmer[]>('/farmers/outstanding-balances')
  },

  getFarmerBalances(farmerId: number): Promise<FarmerBalances> {
    return request<FarmerBalances>(`/farmers/${farmerId}/balances`)
  },

  getFarmerStatement(
    farmerId: number,
    filters: StatementFilters = {},
  ): Promise<StatementLine[]> {
    return request<StatementLine[]>(
      `/farmers/${farmerId}/statement${buildQuery({ ...filters })}`,
    )
  },

  getFarmerPayments(
    farmerId: number,
    filters: PaymentHistoryFilters = {},
  ): Promise<PagedResponse<FarmerPayment>> {
    const { page = 0, size = 20, ...rest } = filters
    return request<PagedResponse<FarmerPayment>>(
      `/farmers/${farmerId}/payments${buildQuery({ ...rest, page, size })}`,
    )
  },

  /** Record a full or partial payment. The backend validates the balance. */
  recordPayment(payload: RecordPaymentRequest): Promise<FarmerPayment> {
    return request<FarmerPayment>('/payments', { method: 'POST', json: payload })
  },
}
