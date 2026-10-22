import type {
  CreateGradeRequest,
  GradeDefinition,
  UpdateGradeRequest,
} from '@/types'
import { ApiError, USE_MOCKS, mockDelay, request } from './api'

/** In-memory mock grades (fallback only; real data comes from the backend). */
const mockGrades: GradeDefinition[] = [
  { id: 1, productId: 1, gradeCode: 'GRADE_A', name: 'Grade A', description: 'Premium quality', displayOrder: 0, active: true },
  { id: 2, productId: 1, gradeCode: 'GRADE_B', name: 'Grade B', description: 'Minor blemishes', displayOrder: 1, active: true },
  { id: 3, productId: 1, gradeCode: 'GRADE_C', name: 'Grade C', description: 'Processing grade', displayOrder: 2, active: true },
]

export const gradeService = {
  async listByProduct(productId: number): Promise<GradeDefinition[]> {
    if (USE_MOCKS) {
      return mockDelay(
        mockGrades
          .filter((g) => g.productId === productId)
          .sort((a, b) => a.displayOrder - b.displayOrder),
      )
    }
    return request<GradeDefinition[]>(`/products/${productId}/grades`)
  },

  async create(
    productId: number,
    body: CreateGradeRequest,
  ): Promise<GradeDefinition> {
    if (USE_MOCKS) {
      const dup = mockGrades.some(
        (g) => g.productId === productId && g.gradeCode === body.gradeCode,
      )
      if (dup) {
        throw new ApiError(409, `Grade code '${body.gradeCode}' already exists for this product`)
      }
      const created: GradeDefinition = {
        id: Math.max(0, ...mockGrades.map((g) => g.id)) + 1,
        productId,
        gradeCode: body.gradeCode,
        name: body.name,
        description: body.description,
        displayOrder: body.displayOrder,
        active: body.active ?? true,
      }
      mockGrades.push(created)
      return mockDelay(created)
    }
    return request<GradeDefinition>(`/products/${productId}/grades`, {
      method: 'POST',
      json: body,
    })
  },

  async update(
    gradeId: number,
    body: UpdateGradeRequest,
  ): Promise<GradeDefinition> {
    if (USE_MOCKS) {
      const grade = mockGrades.find((g) => g.id === gradeId)
      if (!grade) throw new ApiError(404, 'Grade not found')
      grade.name = body.name
      grade.description = body.description
      grade.displayOrder = body.displayOrder
      grade.active = body.active
      return mockDelay(grade)
    }
    return request<GradeDefinition>(`/grades/${gradeId}`, {
      method: 'PUT',
      json: body,
    })
  },
}
