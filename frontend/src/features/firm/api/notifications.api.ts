import { http } from '@/shared/lib/http'

export type NotificationItem = {
  id: number
  type: string
  title: string
  message: string
  read: boolean
  createdAt?: string
}

type PageResponse<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export async function listMyNotifications(page = 0, size = 20): Promise<PageResponse<NotificationItem>> {
  const response = await http.get<PageResponse<NotificationItem>>('/api/notifications/me', {
    params: { page, size },
  })
  return response.data
}

export async function getMyUnreadNotificationCount(): Promise<number> {
  const response = await http.get<number>('/api/notifications/me/unread-count')
  return response.data
}

export async function markNotificationAsRead(id: number): Promise<NotificationItem> {
  const response = await http.patch<NotificationItem>(`/api/notifications/${id}/read`)
  return response.data
}
