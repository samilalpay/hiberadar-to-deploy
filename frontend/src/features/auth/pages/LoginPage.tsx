import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/features/auth/model/use-auth'
import { http } from '@/shared/lib/http'

type LoginResponse = {
  accessToken: string
  username: string
  role: 'FIRMA' | 'ADMIN' | 'TEKNOPARK'
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation() as { state?: { from?: string } }
  const { login } = useAuth()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')

  function resolvePostLoginPath(role: LoginResponse['role'], from?: string): string {
    const safeFrom = typeof from === 'string' ? from : ''
    if (role === 'FIRMA') {
      return safeFrom.startsWith('/app/') || safeFrom === '/app' ? safeFrom : '/app/dashboard'
    }
    return safeFrom.startsWith('/admin/') || safeFrom === '/admin' ? safeFrom : '/admin/dashboard'
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)
    setError('')

    try {
      const response = await http.post<LoginResponse>('/api/auth/login', {
        username,
        password,
      })

      const data = response.data
      login({ token: data.accessToken, username: data.username, role: data.role })
      navigate(resolvePostLoginPath(data.role, location.state?.from), { replace: true })
    } catch {
      setError('Kullanıcı adı veya şifre hatalı.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="auth-shell">
      <div className="auth-card auth-card-compact">
        <div className="auth-head">
          <p className="auth-eyebrow">HibeRadar</p>
          <h1>Giris Yap</h1>
          <p>Panelinize erismek icin bilgilerinizi girin.</p>
        </div>
        <form onSubmit={onSubmit} className="auth-form">
          <label>
            Kullanici adi
            <input placeholder="Kullanici adiniz" value={username} onChange={(e) => setUsername(e.target.value)} />
          </label>
          <label>
            Sifre
            <input
              type="password"
              placeholder="Sifreniz"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </label>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Giris Yapiliyor...' : 'Giris Yap'}
          </button>
          {error ? <p className="panel-error">{error}</p> : null}
          <p className="auth-help">
            Hesabin yok mu? <a href="/register">Kayit Ol</a>
          </p>
        </form>
      </div>
    </section>
  )
}
