import { env } from '@/shared/config/env'

export function resolveMediaUrl(value?: string | null): string | undefined {
  if (!value) return undefined
  const trimmed = value.trim()
  if (!trimmed) return undefined
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) return trimmed
  if (trimmed.startsWith('data:')) return trimmed
  if (trimmed.startsWith('/')) return `${env.apiBaseUrl}${trimmed}`
  return `${env.apiBaseUrl}/${trimmed}`
}
