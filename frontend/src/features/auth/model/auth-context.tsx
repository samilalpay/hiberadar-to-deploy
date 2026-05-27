import { createContext, useMemo, useState, type ReactNode } from 'react'
import { clearStoredAuth, getStoredAuth, setStoredAuth, type StoredAuth } from '@/shared/lib/storage'

type Role = StoredAuth['role']

type AuthContextValue = {
  token: string | null
  role: Role
  username: string | null
  isAuthenticated: boolean
  login: (payload: Omit<StoredAuth, 'role'> & { role: Exclude<Role, 'GUEST'> }) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export { AuthContext }

export function AuthProvider({ children }: { children: ReactNode }) {
  const initial = getStoredAuth()

  const [token, setToken] = useState<string | null>(initial?.token ?? null)
  const [role, setRole] = useState<Role>(initial?.role ?? 'GUEST')
  const [username, setUsername] = useState<string | null>(initial?.username ?? null)

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      role,
      username,
      isAuthenticated: Boolean(token),
      login: (payload) => {
        const auth: StoredAuth = {
          token: payload.token,
          role: payload.role,
          username: payload.username,
        }
        setStoredAuth(auth)
        setToken(auth.token)
        setRole(auth.role)
        setUsername(auth.username)
      },
      logout: () => {
        clearStoredAuth()
        setToken(null)
        setRole('GUEST')
        setUsername(null)
      },
    }),
    [token, role, username],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
