import type {
  CreateFarmerRequest,
  Farmer,
  ListParams,
  PagedResponse,
  UpdateFarmerRequest,
} from '@/types'
import { ApiError, USE_MOCKS, buildQuery, mockDelay, request } from './api'
import { farmers } from './mockData'

/** Client-side paginate + search over the mock farmers (fallback only). */
function mockList({ search, page = 0, size = 10 }: ListParams): PagedResponse<Farmer> {
  const q = search?.trim().toLowerCase() ?? ''
  const matched = q
    ? farmers.filter((f) =>
        [f.farmerCode, f.name, f.phone, f.village]
          .join(' ')
          .toLowerCase()
          .includes(q),
      )
    : farmers
  const start = page * size
  return {
    content: matched.slice(start, start + size),
    totalElements: matched.length,
    totalPages: Math.max(1, Math.ceil(matched.length / size)),
    number: page,
    size,
  }
}

export const farmerService = {
  async list(params: ListParams = {}): Promise<PagedResponse<Farmer>> {
    if (USE_MOCKS) return mockDelay(mockList(params))
    const { search, page = 0, size = 10 } = params
    return request<PagedResponse<Farmer>>(
      `/farmers${buildQuery({ search, page, size })}`,
    )
  },

  async getById(id: number): Promise<Farmer> {
    if (USE_MOCKS) {
      const found = farmers.find((f) => f.id === id)
      if (!found) throw new ApiError(404, 'Farmer not found')
      return mockDelay(found)
    }
    return request<Farmer>(`/farmers/${id}`)
  },

  async create(body: CreateFarmerRequest): Promise<Farmer> {
    if (USE_MOCKS) {
      const code = body.farmerCode?.trim()
      if (code && farmers.some((f) => f.farmerCode === code)) {
        throw new ApiError(409, `Farmer code '${code}' already exists`)
      }
      const now = new Date().toISOString()
      const created: Farmer = {
        id: Math.max(0, ...farmers.map((f) => f.id)) + 1,
        farmerCode: code || `F-${String(farmers.length + 1).padStart(5, '0')}`,
        name: body.name,
        phone: body.phone,
        village: body.village,
        status: 'ACTIVE',
        createdAt: now,
        updatedAt: now,
      }
      farmers.unshift(created)
      return mockDelay(created)
    }
    return request<Farmer>('/farmers', { method: 'POST', json: body })
  },

  async update(id: number, body: UpdateFarmerRequest): Promise<Farmer> {
    if (USE_MOCKS) {
      const farmer = farmers.find((f) => f.id === id)
      if (!farmer) throw new ApiError(404, 'Farmer not found')
      farmer.name = body.name
      farmer.phone = body.phone
      farmer.village = body.village
      if (body.status) farmer.status = body.status
      farmer.updatedAt = new Date().toISOString()
      return mockDelay(farmer)
    }
    return request<Farmer>(`/farmers/${id}`, { method: 'PUT', json: body })
  },
}
