import { http } from '@/shared/lib/http'
import type { GrantItem } from '@/features/firm/api/grants.api'
import type { GrantDetail } from '@/features/firm/api/grants.api'

export type SourceItem = {
  id: number
  name: string
  category?: string
  scope?: string
  countryCode?: string
  officialUrl?: string
  notes?: string
  active: boolean
}

export type PageResponse<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminApplication = {
  id: number
  grantId: number
  grantTitle?: string
  firmUsername?: string
  status: 'SUBMITTED' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED'
  submittedAt?: string
  decidedAt?: string
  decisionNote?: string
  requestedMeetingAt?: string
  confirmedMeetingAt?: string
  meetingNote?: string
}

export type MeetingCalendarItem = {
  applicationId: number
  grantId?: number
  grantTitle?: string
  firmUsername?: string
  requestedMeetingAt?: string
  confirmedMeetingAt?: string
  meetingNote?: string
  effectiveMeetingAt?: string
  meetingStatus?: 'REQUESTED' | 'CONFIRMED' | 'REJECTED'
  submittedAt?: string
  decidedAt?: string
  updatedAt?: string
}

export type AdminFirmRegistration = {
  id: number
  username: string
  email: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt?: string
  decidedAt?: string
  decisionNote?: string
  createdBy?: string
}

export type ProfileResponse = {
  username: string
  email: string
  role: string
  profileCompleted: boolean
  applicantType: string
  companyAgeMonths: number
  employees: number
  countryCode: string
  cofundingAvailable: boolean
  cofundingRate: number
  sector: string
  activityArea: string
  turnover: number
  naceCodes: string
}

export type UpdateProfilePayload = {
  applicantType: string
  companyAgeMonths: number
  employees: number
  countryCode: string
  cofundingAvailable: boolean
  cofundingRate: number
  sector: string
  activityArea: string
  turnover: number
  naceCodes: string
}

export type ChangePasswordPayload = {
  currentPassword: string
  newPassword: string
}

export async function listAdminGrants(params: {
  q?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'CLOSED'
  countryCode?: string
  page?: number
  size?: number
}): Promise<PageResponse<GrantItem>> {
  const response = await http.get<PageResponse<GrantItem>>('/api/admin/grants', {
    params: {
      q: params.q?.trim() || undefined,
      status: params.status,
      countryCode: params.countryCode?.trim() || undefined,
      page: params.page ?? 0,
      size: params.size ?? 24,
    },
  })
  return response.data
}

export async function getAdminGrantDetail(id: number): Promise<GrantDetail> {
  const response = await http.get<GrantDetail>(`/api/admin/grants/${id}`)
  return response.data
}

export async function updateAdminGrant(
  id: number,
  payload: {
    title: string
    scope: 'NATIONAL' | 'INTERNATIONAL'
    naceCode?: string
    countryCode?: string
    officialUrl?: string
    providerName?: string
    programName?: string
    referenceCode?: string
    summaryShort?: string
    adminQuickInfo?: string
    deadlineAt?: string
    currency?: string
    fundingMin?: number
    fundingMax?: number
  },
) {
  const response = await http.put(`/api/admin/grants/${id}`, payload)
  return response.data
}

export async function setAdminGrantActive(id: number, active: boolean) {
  const response = await http.patch(`/api/admin/grants/${id}/active`, undefined, {
    params: { active },
  })
  return response.data
}

export async function deleteAdminGrant(id: number) {
  const response = await http.delete(`/api/admin/grants/${id}`)
  return response.data
}

export async function setAdminGrantStatus(id: number, status: 'DRAFT' | 'PUBLISHED' | 'CLOSED') {
  const response = await http.patch(`/api/admin/grants/${id}/status`, { status })
  return response.data
}

export async function listAdminApplications(params: {
  status?: 'SUBMITTED' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED'
  search?: string
  page?: number
  size?: number
}): Promise<PageResponse<AdminApplication>> {
  const response = await http.get<PageResponse<AdminApplication>>('/api/admin/applications', {
    params: {
      status: params.status,
      // Keep both query keys for compatibility across backend revisions.
      q: params.search?.trim() || undefined,
      firmUsername: params.search?.trim() || undefined,
      page: params.page ?? 0,
      size: params.size ?? 12,
    },
  })
  return response.data
}

export async function listSources(): Promise<SourceItem[]> {
  const response = await http.get<SourceItem[]>('/api/sources')
  return response.data
}

export async function createAdminGrant(payload: {
  title: string
  scope: 'NATIONAL' | 'INTERNATIONAL'
  naceCode?: string
  countryCode?: string
  officialUrl?: string
  providerName?: string
  programName?: string
  referenceCode?: string
  summaryShort?: string
  adminQuickInfo?: string
  deadlineAt?: string
  currency?: string
  fundingMin?: number
  fundingMax?: number
}) {
  const response = await http.post('/api/admin/grants', payload)
  return response.data
}

export async function setAdminApplicationStatus(
  id: number,
  status: 'SUBMITTED' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED',
  decisionNote?: string,
) {
  const response = await http.patch(`/api/admin/applications/${id}/status`, {
    status,
    decisionNote: decisionNote?.trim() || undefined,
  })
  return response.data
}

export async function listAdminFirmRegistrations(params: {
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  page?: number
  size?: number
}): Promise<PageResponse<AdminFirmRegistration>> {
  const response = await http.get<PageResponse<AdminFirmRegistration>>('/api/admin/firm-registrations', {
    params: {
      status: params.status,
      page: params.page ?? 0,
      size: params.size ?? 12,
    },
  })
  return response.data
}

export async function setAdminFirmRegistrationStatus(
  id: number,
  status: 'PENDING' | 'APPROVED' | 'REJECTED',
  decisionNote?: string,
) {
  const response = await http.patch(`/api/admin/firm-registrations/${id}/status`, {
    status,
    decisionNote: decisionNote?.trim() || undefined,
  })
  return response.data
}

export async function listMyApplications(page = 0, size = 12): Promise<PageResponse<AdminApplication>> {
  const response = await http.get<PageResponse<AdminApplication>>('/api/applications/me', {
    params: { page, size },
  })
  return response.data
}

export async function listMyMeetingCalendar(): Promise<MeetingCalendarItem[]> {
  const response = await http.get<MeetingCalendarItem[]>('/api/applications/meetings/me')
  return response.data
}

export async function listAdminMeetingCalendar(): Promise<MeetingCalendarItem[]> {
  const response = await http.get<MeetingCalendarItem[]>('/api/admin/applications/meetings')
  return response.data
}

export async function listUnavailableMeetingDays(isAdmin = false): Promise<string[]> {
  const path = isAdmin
    ? '/api/admin/applications/meetings/unavailable-days'
    : '/api/applications/meetings/unavailable-days'
  const response = await http.get<string[]>(path)
  return response.data
}

export async function listOccupiedMeetingTimes(): Promise<string[]> {
  const response = await http.get<string[]>('/api/applications/meetings/occupied-times')
  return response.data
}

export async function requestMeeting(applicationId: number, requestedMeetingAt: string, note?: string) {
  const response = await http.patch(`/api/applications/${applicationId}/meeting`, {
    requestedMeetingAt,
    note: note?.trim() || undefined,
  })
  return response.data
}

export async function requestMeetingDirect(requestedMeetingAt: string, note?: string) {
  try {
    const response = await http.patch('/api/applications/meeting', {
      requestedMeetingAt,
      note: note?.trim() || undefined,
    })
    return response.data
  } catch (error) {
    // Backward compatibility: if backend is an older build without /api/applications/meeting,
    // use the first available application with the legacy endpoint.
    if (typeof error === 'object' && error && 'response' in error) {
      const responseLike = error as { response?: { status?: number } }
      if (responseLike.response?.status === 404) {
        const applications = await listMyApplications(0, 1)
        const firstApplicationId = applications.content[0]?.id
        if (!firstApplicationId) {
          throw error
        }
        return requestMeeting(firstApplicationId, requestedMeetingAt, note)
      }
    }
    throw error
  }
}

export async function confirmMeeting(
  applicationId: number,
  confirmedMeetingAt?: string,
  note?: string,
  decision: 'APPROVE' | 'REJECT' = 'APPROVE',
) {
  const payload: { confirmedMeetingAt?: string; note?: string; decision?: 'APPROVE' | 'REJECT' } = {
    note: note?.trim() || undefined,
    decision,
  }
  if (confirmedMeetingAt) {
    payload.confirmedMeetingAt = confirmedMeetingAt
  }
  const response = await http.patch(`/api/admin/applications/${applicationId}/meeting`, payload)
  return response.data
}

export async function rejectMeeting(applicationId: number, note?: string) {
  const response = await http.patch(`/api/admin/applications/${applicationId}/meeting/reject`, {
    note: note?.trim() || undefined,
  })
  return response.data
}

export async function cancelMeeting(applicationId: number, note?: string) {
  const response = await http.patch(`/api/admin/applications/${applicationId}/meeting/cancel`, {
    note: note?.trim() || undefined,
  })
  return response.data
}

export async function listMyMatchedGrants(page = 0, size = 12): Promise<PageResponse<GrantItem>> {
  const response = await http.get<PageResponse<GrantItem>>('/api/grants/matches/me', {
    params: { page, size },
  })
  return response.data
}

export async function getMyProfile(): Promise<ProfileResponse> {
  const response = await http.get<ProfileResponse>('/api/profile/me')
  return response.data
}

export async function updateMyProfile(payload: UpdateProfilePayload): Promise<ProfileResponse> {
  const response = await http.put<ProfileResponse>('/api/profile/me', payload)
  return response.data
}

export async function changeMyPassword(payload: ChangePasswordPayload): Promise<{ message: string }> {
  const response = await http.patch<{ message: string }>('/api/profile/me/password', payload)
  return response.data
}
