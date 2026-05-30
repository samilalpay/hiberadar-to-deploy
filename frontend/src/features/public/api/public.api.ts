import { http } from '@/shared/lib/http'

export type GrantItem = {
  id: number
  title: string
  providerName?: string
  programName?: string
  referenceCode?: string
  summaryShort?: string
  publishedAt?: string
  deadlineAt?: string
  naceCode?: string
  countryCode?: string
  scope?: 'NATIONAL' | 'INTERNATIONAL'
  currency?: string
  fundingMin?: number
  fundingMax?: number
  status?: 'DRAFT' | 'PUBLISHED' | 'CLOSED'
  clickable?: boolean
}

export type GrantDetail = GrantItem & {
  summaryShort?: string
  adminQuickInfo?: string
  officialUrl?: string
}

export type GrantEligibilityRule = {
  id: number
  grantId: number
  applicantTypes: string[]
  minCompanyAgeMonths?: number
  minEmployees?: number
  maxEmployees?: number
  minTurnover?: number
  maxTurnover?: number
  trlMin?: number
  trlMax?: number
  requiredCountryCodes: string[]
  cofundingRequired?: boolean
  cofundingRate?: number
  notes?: string
}

type PageResponse<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

type ListGrantsParams = {
  q?: string
  sourceId?: number
  nace?: string
  scope?: 'NATIONAL' | 'INTERNATIONAL'
  countryCode?: string
  currency?: string
  deadlineFrom?: string
  deadlineTo?: string
  minFunding?: number
  maxFunding?: number
  status?: 'PUBLISHED' | 'CLOSED'
  page?: number
  size?: number
}

export type EligibilityCheckRequest = {
  grantId: number
  applicantType?: string
  companyAgeMonths?: number
  employees?: number
  turnover?: number
  countryCode?: string
  cofundingAvailable?: boolean
  cofundingRate?: number
}

export type EligibilityCheckResponse = {
  eligible: boolean
  reasons: string[]
}

export type RegisterFirmRequest = {
  username: string
  firstName: string
  lastName: string
  phone: string
  email: string
  password: string
  note?: string
}

export type RegisterFirmResponse = {
  requestId: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
}

export async function listGrants(params: ListGrantsParams): Promise<PageResponse<GrantItem>> {
  const response = await http.get<PageResponse<GrantItem>>('/api/grants', {
    params: {
      status: params.status,
      page: params.page ?? 0,
      size: params.size ?? 12,
      q: params.q?.trim() || undefined,
      sourceId: params.sourceId,
      nace: params.nace?.trim() || undefined,
      scope: params.scope,
      countryCode: params.countryCode?.trim() || undefined,
      currency: params.currency?.trim() || undefined,
      deadlineFrom: params.deadlineFrom || undefined,
      deadlineTo: params.deadlineTo || undefined,
      minFunding: params.minFunding,
      maxFunding: params.maxFunding,
    },
  })

  return response.data
}

export async function listPublishedGrants(query: string, size = 10): Promise<PageResponse<GrantItem>> {
  return listGrants({ q: query, status: 'PUBLISHED', page: 0, size })
}

export async function getGrantDetail(id: number): Promise<GrantDetail> {
  const response = await http.get<GrantDetail>(`/api/grants/${id}`)
  return response.data
}

export async function getGrantEligibilityRule(id: number): Promise<GrantEligibilityRule> {
  const response = await http.get<GrantEligibilityRule>(`/api/grants/${id}/eligibility`)
  return response.data
}

export async function checkEligibility(payload: EligibilityCheckRequest): Promise<EligibilityCheckResponse> {
  const response = await http.post<EligibilityCheckResponse>('/api/eligibility/check', payload)
  return response.data
}

export async function createFirmRegistration(payload: RegisterFirmRequest): Promise<RegisterFirmResponse> {
  const response = await http.post<RegisterFirmResponse>('/api/firm-registrations', {
    username: payload.username.trim(),
    firstName: payload.firstName.trim(),
    lastName: payload.lastName.trim(),
    phone: payload.phone.trim(),
    email: payload.email.trim(),
    password: payload.password,
    note: payload.note?.trim() || undefined,
  })
  return response.data
}
