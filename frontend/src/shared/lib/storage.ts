const AUTH_KEY = 'hiberadar_auth'

export type StoredAuth = {
  token: string
  role: 'GUEST' | 'FIRMA' | 'ADMIN' | 'TEKNOPARK'
  username: string
}

export function getStoredAuth(): StoredAuth | null {
  const raw = localStorage.getItem(AUTH_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredAuth
  } catch {
    localStorage.removeItem(AUTH_KEY)
    return null
  }
}

export function setStoredAuth(value: StoredAuth): void {
  localStorage.setItem(AUTH_KEY, JSON.stringify(value))
}

export function clearStoredAuth(): void {
  localStorage.removeItem(AUTH_KEY)
}
