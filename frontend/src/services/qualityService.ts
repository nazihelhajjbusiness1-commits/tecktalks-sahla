import type { QualitySummary } from '@/types'
import { USE_MOCKS, mockDelay, request } from './api'

/** Mock QC summary (fallback only; real data comes from /api/quality/summary). */
const mockSummary: QualitySummary = {
  totalGraded: 46,
  rejectCount: 3,
  rejectRate: 3 / 46,
  distribution: [
    { gradeCode: 'GRADE_A', gradeName: 'Grade A', count: 22 },
    { gradeCode: 'GRADE_B', gradeName: 'Grade B', count: 13 },
    { gradeCode: 'GRADE_C', gradeName: 'Grade C', count: 8 },
    { gradeCode: 'REJECT', gradeName: 'Reject', count: 3 },
  ],
}

export const qualityService = {
  /** Grade distribution + reject rate across all graded deliveries. */
  async summary(): Promise<QualitySummary> {
    if (USE_MOCKS) return mockDelay(mockSummary)
    return request<QualitySummary>('/quality/summary')
  },
}
