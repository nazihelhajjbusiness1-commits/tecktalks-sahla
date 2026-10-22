import type {
  Currency,
  DailyCollectionRow,
  InventoryReportRow,
  OutstandingPaymentsRow,
  StatementLine,
} from '@/types'
import { buildQuery, request } from './api'

export interface DailyCollectionParams {
  from?: string
  to?: string
  farmerId?: number
  productId?: number
}

export interface InventoryReportParams {
  productId?: number
  gradeId?: number
}

export interface StatementParams {
  currency?: Currency
  from?: string
  to?: string
}

/**
 * DT-91: operational + financial reports. Daily collection, inventory and
 * outstanding payments come from /api/reports/*; the farmer statement reuses
 * the existing /api/farmers/{id}/statement endpoint (DT-86).
 */
export const reportService = {
  dailyCollection(params: DailyCollectionParams = {}): Promise<DailyCollectionRow[]> {
    return request<DailyCollectionRow[]>(`/reports/daily-collection${buildQuery({ ...params })}`)
  },

  inventory(params: InventoryReportParams = {}): Promise<InventoryReportRow[]> {
    return request<InventoryReportRow[]>(`/reports/inventory${buildQuery({ ...params })}`)
  },

  outstandingPayments(): Promise<OutstandingPaymentsRow[]> {
    return request<OutstandingPaymentsRow[]>('/reports/outstanding-payments')
  },

  farmerStatement(farmerId: number, params: StatementParams = {}): Promise<StatementLine[]> {
    return request<StatementLine[]>(`/farmers/${farmerId}/statement${buildQuery({ ...params })}`)
  },
}
