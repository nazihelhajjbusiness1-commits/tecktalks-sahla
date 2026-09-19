import type { Farmer } from '@/types'
import { USE_MOCKS, mockDelay, request } from './api'
import { farmers } from './mockData'

interface BackendFarmer {
  id: number
  farmerCode: string
  name: string
  phone: string
  village: string
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
}

interface BackendPage<T> {
  content: T[]
}

// The real Farmer entity (Farmer.java) has no region/mainCrop/totalDeliveries/
// balance - those only exist in the Week 1 mock schema. Defaulted here rather
// than left undefined so the UI doesn't render "undefined" everywhere; see
// the QA bug report for the underlying schema mismatch.
function mapFarmer(f: BackendFarmer): Farmer {
  return {
    id: String(f.id),
    name: f.name,
    village: f.village,
    region: '',
    phone: f.phone,
    mainCrop: '',
    totalDeliveries: 0,
    balance: 0,
    status: f.status === 'ACTIVE' ? 'active' : 'inactive',
    joinedAt: f.createdAt,
  }
}

export const farmerService = {
  async list(): Promise<Farmer[]> {
    if (USE_MOCKS) return mockDelay(farmers)
    const page = await request<BackendPage<BackendFarmer>>('/farmers?size=100')
    return page.content.map(mapFarmer)
  },

  async getById(id: string): Promise<Farmer | undefined> {
    if (USE_MOCKS) return mockDelay(farmers.find((f) => f.id === id))
    const backendFarmer = await request<BackendFarmer>(`/farmers/${id}`)
    return mapFarmer(backendFarmer)
  },
}
