import type {
  CreateProductRequest,
  ListParams,
  PagedResponse,
  Product,
  UpdateProductRequest,
} from '@/types'
import { ApiError, USE_MOCKS, buildQuery, mockDelay, request } from './api'
import { products } from './mockData'

function mockList({ search, page = 0, size = 10 }: ListParams): PagedResponse<Product> {
  const q = search?.trim().toLowerCase() ?? ''
  const matched = q
    ? products.filter((p) =>
        `${p.name} ${p.variety}`.toLowerCase().includes(q),
      )
    : products
  const start = page * size
  return {
    content: matched.slice(start, start + size),
    totalElements: matched.length,
    totalPages: Math.max(1, Math.ceil(matched.length / size)),
    number: page,
    size,
  }
}

export const productService = {
  async list(params: ListParams = {}): Promise<PagedResponse<Product>> {
    if (USE_MOCKS) return mockDelay(mockList(params))
    const { search, page = 0, size = 100 } = params
    return request<PagedResponse<Product>>(
      `/products${buildQuery({ search, page, size })}`,
    )
  },

  async getById(id: number): Promise<Product> {
    if (USE_MOCKS) {
      const found = products.find((p) => p.id === id)
      if (!found) throw new ApiError(404, 'Product not found')
      return mockDelay(found)
    }
    return request<Product>(`/products/${id}`)
  },

  async create(body: CreateProductRequest): Promise<Product> {
    if (USE_MOCKS) {
      const dup = products.some(
        (p) =>
          p.name.toLowerCase() === body.name.trim().toLowerCase() &&
          p.variety.toLowerCase() === body.variety.trim().toLowerCase(),
      )
      if (dup) {
        throw new ApiError(409, 'A product with this name and variety already exists')
      }
      const now = new Date().toISOString()
      const created: Product = {
        id: Math.max(0, ...products.map((p) => p.id)) + 1,
        name: body.name,
        variety: body.variety,
        unit: body.unit,
        active: true,
        createdAt: now,
        updatedAt: now,
      }
      products.push(created)
      return mockDelay(created)
    }
    return request<Product>('/products', { method: 'POST', json: body })
  },

  async update(id: number, body: UpdateProductRequest): Promise<Product> {
    if (USE_MOCKS) {
      const product = products.find((p) => p.id === id)
      if (!product) throw new ApiError(404, 'Product not found')
      product.name = body.name
      product.variety = body.variety
      product.unit = body.unit
      product.active = body.active
      product.updatedAt = new Date().toISOString()
      return mockDelay(product)
    }
    return request<Product>(`/products/${id}`, { method: 'PUT', json: body })
  },
}
