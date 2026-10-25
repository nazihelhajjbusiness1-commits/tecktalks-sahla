import type {
  CreateGradeSuggestionRequest,
  GradeSuggestionResponse,
} from '@/types'
import { request } from './api'

/**
 * Grade suggestions (second-opinion re-grade requests). Inspectors raise them on
 * a delivery; managers review a pending queue and approve (overriding the grade)
 * or reject. Talks to the real backend — no mock fallback.
 */
export const gradeSuggestionService = {
  /** Suggestions raised on a single delivery (newest first). */
  listForDelivery(deliveryId: number): Promise<GradeSuggestionResponse[]> {
    return request<GradeSuggestionResponse[]>(`/deliveries/${deliveryId}/grade-suggestions`)
  },

  /** Inspector raises a suggestion against a delivery's current grade. */
  create(
    deliveryId: number,
    body: CreateGradeSuggestionRequest,
  ): Promise<GradeSuggestionResponse> {
    return request<GradeSuggestionResponse>(`/deliveries/${deliveryId}/grade-suggestions`, {
      method: 'POST',
      json: body,
    })
  },

  /** Manager queue: all PENDING suggestions awaiting review. */
  listPending(): Promise<GradeSuggestionResponse[]> {
    return request<GradeSuggestionResponse[]>('/grade-suggestions')
  },

  pendingCount(): Promise<{ count: number }> {
    return request<{ count: number }>('/grade-suggestions/pending-count')
  },

  /** Approve → overrides the delivery's grade to the suggested one. */
  approve(id: number, reviewNotes?: string): Promise<GradeSuggestionResponse> {
    return request<GradeSuggestionResponse>(`/grade-suggestions/${id}/approve`, {
      method: 'POST',
      json: { reviewNotes: reviewNotes?.trim() || undefined },
    })
  },

  reject(id: number, reviewNotes?: string): Promise<GradeSuggestionResponse> {
    return request<GradeSuggestionResponse>(`/grade-suggestions/${id}/reject`, {
      method: 'POST',
      json: { reviewNotes: reviewNotes?.trim() || undefined },
    })
  },
}
