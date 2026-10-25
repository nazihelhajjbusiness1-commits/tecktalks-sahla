import type {
  CaptureGradeRequest,
  CaptureWeightRequest,
  CreateDeliveryRequest,
  DeliveryAuditEntry,
  DeliveryGradeResponse,
  DeliveryListParams,
  DeliveryPhotoResponse,
  DeliveryResponse,
  DeliveryWeightResponse,
  PagedResponse,
} from '@/types'
import { buildQuery, request, requestBlob, uploadForm } from './api'

/**
 * Delivery intake API (Sprint 3, DT-58–DT-61).
 *
 * Talks to the real Spring Boot backend — the delivery workflow has live
 * endpoints now, so there is no mock fallback (per the Sprint 3 rule: no mock
 * delivery data once the API exists). All calls go through the typed `request`
 * helper so components never touch `fetch` directly.
 */
export const deliveryService = {
  /** DT-58: paginated + filtered delivery list. */
  list(params: DeliveryListParams = {}): Promise<PagedResponse<DeliveryResponse>> {
    const { farmerId, productId, status, from, to, page = 0, size = 20 } = params
    return request<PagedResponse<DeliveryResponse>>(
      `/deliveries${buildQuery({ farmerId, productId, status, from, to, page, size })}`,
    )
  },

  getById(id: number): Promise<DeliveryResponse> {
    return request<DeliveryResponse>(`/deliveries/${id}`)
  },

  /** DT-59: create the draft delivery; the returned id drives later steps. */
  create(body: CreateDeliveryRequest): Promise<DeliveryResponse> {
    return request<DeliveryResponse>('/deliveries', { method: 'POST', json: body })
  },

  /** DT-60: capture gross/tare; backend computes and returns the net weight. */
  captureWeight(
    id: number,
    body: CaptureWeightRequest,
  ): Promise<DeliveryWeightResponse> {
    return request<DeliveryWeightResponse>(`/deliveries/${id}/weight`, {
      method: 'POST',
      json: body,
    })
  },

  getWeight(id: number): Promise<DeliveryWeightResponse> {
    return request<DeliveryWeightResponse>(`/deliveries/${id}/weight`)
  },

  /** DT-60: assign a product-specific manual grade. */
  captureGrade(
    id: number,
    body: CaptureGradeRequest,
  ): Promise<DeliveryGradeResponse> {
    return request<DeliveryGradeResponse>(`/deliveries/${id}/grade`, {
      method: 'POST',
      json: body,
    })
  },

  getGrade(id: number): Promise<DeliveryGradeResponse> {
    return request<DeliveryGradeResponse>(`/deliveries/${id}/grade`)
  },

  /** DT-61: explicit confirmation — commits stock via one inventory movement. */
  confirm(id: number): Promise<DeliveryResponse> {
    return request<DeliveryResponse>(`/deliveries/${id}/confirm`, { method: 'POST' })
  },

  /** DT-57/DT-61: delivery audit history (newest first). */
  auditHistory(id: number): Promise<DeliveryAuditEntry[]> {
    return request<DeliveryAuditEntry[]>(`/deliveries/${id}/history`)
  },

  /** Produce photos (inspection evidence): metadata list, oldest first. */
  listPhotos(id: number): Promise<DeliveryPhotoResponse[]> {
    return request<DeliveryPhotoResponse[]>(`/deliveries/${id}/photos`)
  },

  /** Attach a produce photo (multipart). Receiving/inspector/manager/admin only. */
  uploadPhoto(id: number, file: File): Promise<DeliveryPhotoResponse> {
    const form = new FormData()
    form.append('file', file)
    return uploadForm<DeliveryPhotoResponse>(`/deliveries/${id}/photos`, form)
  },

  deletePhoto(id: number, photoId: number): Promise<void> {
    return request<void>(`/deliveries/${id}/photos/${photoId}`, { method: 'DELETE' })
  },

  /**
   * Fetch one photo's bytes (behind JWT auth) and return an object URL for an
   * `<img>`. Callers must `URL.revokeObjectURL` it when done to free memory.
   */
  async photoObjectUrl(id: number, photoId: number): Promise<string> {
    const blob = await requestBlob(`/deliveries/${id}/photos/${photoId}`)
    return URL.createObjectURL(blob)
  },
}
