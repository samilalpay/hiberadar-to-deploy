import { http } from '@/shared/lib/http'

export type PreAnalysisItem = {
  id: number
  firmUsername?: string
  activityArea: string
  machinePark?: string
  investmentPlan?: string
  rdExperience?: string
  exportStatus?: string
  financialCapacity?: string
  firmNote?: string
  status: string
  reviewNote?: string
  reportSummary?: string
  reviewedBy?: string
  submittedAt?: string
  reviewedAt?: string
}

export type CreatePreAnalysisPayload = {
  activityArea: string
  machinePark?: string
  investmentPlan?: string
  rdExperience?: string
  exportStatus?: string
  financialCapacity?: string
  note?: string
}

type PageResponse<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export async function listMyPreAnalysis(page = 0, size = 20): Promise<PageResponse<PreAnalysisItem>> {
  const response = await http.get<PageResponse<PreAnalysisItem>>('/api/pre-analysis/me', {
    params: { page, size },
  })
  return response.data
}

export async function createPreAnalysis(payload: CreatePreAnalysisPayload): Promise<PreAnalysisItem> {
  const response = await http.post<PreAnalysisItem>('/api/pre-analysis', {
    activityArea: payload.activityArea.trim(),
    machinePark: payload.machinePark?.trim() || undefined,
    investmentPlan: payload.investmentPlan?.trim() || undefined,
    rdExperience: payload.rdExperience?.trim() || undefined,
    exportStatus: payload.exportStatus?.trim() || undefined,
    financialCapacity: payload.financialCapacity?.trim() || undefined,
    note: payload.note?.trim() || undefined,
  })
  return response.data
}
