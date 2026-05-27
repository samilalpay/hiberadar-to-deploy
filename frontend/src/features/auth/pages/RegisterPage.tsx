import { useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { Link } from 'react-router-dom'
import { createFirmRegistration } from '@/features/public/api/public.api'

type ApiErrorBody = {
  message?: string
  error?: string
}

export function RegisterPage() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [note, setNote] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSuccess('')

    if (password !== confirmPassword) {
      setError('Sifre tekrari sifre ile ayni olmali.')
      return
    }

    setIsSubmitting(true)
    try {
      const data = await createFirmRegistration({
        username: username.trim(),
        email: email.trim(),
        password,
        note: note.trim() || undefined,
      })
      setSuccess(`Kayit talebiniz alindi (Talep No: ${data.requestId}, Durum: ${data.status}). Admin onayindan sonra giris yapabilirsiniz.`)
      setUsername('')
      setEmail('')
      setPassword('')
      setConfirmPassword('')
      setNote('')
    } catch (err) {
      if (isAxiosError<ApiErrorBody>(err)) {
        const status = err.response?.status
        const detail = err.response?.data?.message || err.response?.data?.error
        setError(`Kayit talebi olusturulamadi${status ? ` (HTTP ${status})` : ''}${detail ? `: ${detail}` : ''}.`)
      } else {
        setError('Kayit talebi olusturulamadi.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="auth-card">
      <h1>Kayit Ol</h1>
      <p className="auth-help">Firma kaydiniz once talep olarak iletilir. Onaylandiginda hesabiniz aktif olur.</p>

      <form onSubmit={onSubmit} className="auth-form">
        <input
          placeholder="Kullanici adi"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          minLength={3}
          maxLength={120}
          required
        />
        <input
          type="email"
          placeholder="E-posta"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          maxLength={180}
          required
        />
        <input
          type="password"
          placeholder="Sifre"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={6}
          maxLength={72}
          required
        />
        <input
          type="password"
          placeholder="Sifre (Tekrar)"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          minLength={6}
          maxLength={72}
          required
        />
        <textarea
          placeholder="Kisa not (opsiyonel)"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          rows={3}
          maxLength={300}
        />

        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? 'Gonderiliyor...' : 'Kayit Talebi Gonder'}
        </button>

        {error ? <p className="panel-error">{error}</p> : null}
        {success ? <p className="panel-success">{success}</p> : null}

        <p className="auth-help">
          Hesabin var mi? <Link to="/login">Giris Yap</Link>
        </p>
      </form>
    </section>
  )
}
