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
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [note, setNote] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const phonePattern = /^5\d{9}$/
  const noteLimit = 300

  function formatPhone(value: string): string {
    const digits = value.replace(/\D/g, '')
    const trimmed = digits.startsWith('0') ? digits.slice(1) : digits
    const limited = trimmed.slice(0, 10)
    const parts = [
      limited.slice(0, 3),
      limited.slice(3, 6),
      limited.slice(6, 8),
      limited.slice(8, 10),
    ].filter(Boolean)
    return parts.join(' ')
  }

  function normalizePhone(value: string): string {
    const digits = value.replace(/\D/g, '')
    return digits.startsWith('0') ? digits.slice(1) : digits
  }

  function getPasswordStrength(value: string): { label: string; score: number; className: string } {
    if (!value) return { label: 'Bos', score: 0, className: 'bos' }
    let score = 0
    if (value.length >= 8) score += 1
    if (value.length >= 12) score += 1
    if (/[A-Z]/.test(value)) score += 1
    if (/[a-z]/.test(value)) score += 1
    if (/[0-9]/.test(value)) score += 1
    if (/[^A-Za-z0-9]/.test(value)) score += 1

    if (score <= 2) return { label: 'Zayif', score, className: 'zayif' }
    if (score <= 4) return { label: 'Orta', score, className: 'orta' }
    if (score >= 6) return { label: 'Cok Guclu', score, className: 'cok-guclu' }
    return { label: 'Guclu', score, className: 'guclu' }
  }

  const strength = getPasswordStrength(password)
  const passwordHints = [
    { label: 'En az 8 karakter', met: password.length >= 8 },
    { label: 'En az 1 buyuk harf', met: /[A-Z]/.test(password) },
    { label: 'En az 1 kucuk harf', met: /[a-z]/.test(password) },
    { label: 'En az 1 sayi', met: /[0-9]/.test(password) },
    { label: 'En az 1 sembol', met: /[^A-Za-z0-9]/.test(password) },
  ]
  const isPasswordMatch = confirmPassword.length > 0 && password === confirmPassword
  const isPasswordMismatch = confirmPassword.length > 0 && password !== confirmPassword
  const passwordRulesMet = passwordHints.every((hint) => hint.met)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSuccess('')

    if (password.length < 8) {
      setError('Sifre en az 8 karakter olmali.')
      return
    }

    if (!passwordRulesMet) {
      setError('Sifre guvenlik kurallarini karsilamali.')
      return
    }

    if (password !== confirmPassword) {
      setError('Sifre tekrari sifre ile ayni olmali.')
      return
    }

    const normalizedPhone = normalizePhone(phone)
    if (!phonePattern.test(normalizedPhone)) {
      setError('Telefon numarasi gecersiz. Ornek: 5422910826')
      return
    }

    if (note.trim().length > noteLimit) {
      setError(`Not en fazla ${noteLimit} karakter olabilir.`)
      return
    }

    setIsSubmitting(true)
    try {
      const data = await createFirmRegistration({
        username: username.trim(),
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        phone: normalizedPhone,
        email: email.trim(),
        password,
        note: note.trim() || undefined,
      })
      setSuccess('Kayit talebiniz alindi. Admin onayindan sonra giris yapabilirsiniz.')
      setUsername('')
      setFirstName('')
      setLastName('')
      setPhone('')
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
    <section className="register-shell register-shell-compact">
      <div className="register-card">
        <div className="register-head">
          <h2>Firma Kayit Talebi</h2>
          <p>Bilgileriniz dogrulama icin iletilecek.</p>
        </div>

        <form onSubmit={onSubmit} className="register-form">
          <label>
            Kullanici adi
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              minLength={3}
              maxLength={120}
              required
            />
          </label>
          <label>
            Ad
            <input
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              maxLength={80}
              required
            />
          </label>
          <label>
            Soyad
            <input
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              maxLength={80}
              required
            />
          </label>
          <label>
            Telefon
            <span className="field-hint">Basinda 0 olmadan yaziniz. Format: 5xx xxx xx xx</span>
            <input
              value={phone}
              onChange={(e) => setPhone(formatPhone(e.target.value))}
              inputMode="tel"
              maxLength={13}
              placeholder="5xx xxx xx xx"
              required
            />
          </label>
          <label>
            E-posta
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              maxLength={180}
              required
            />
          </label>
          <label>
            Sifre
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              maxLength={72}
              required
            />
            <div className="password-strength-row">
              <div className={`strength-meter is-${strength.className}`}>
                <span style={{ width: `${Math.min(100, (strength.score / 6) * 100)}%` }}></span>
              </div>
              <span className={`strength-label is-${strength.className}`}>Gucluk: {strength.label}</span>
              <ul className="password-hint-list">
                {passwordHints.map((hint) => (
                  <li key={hint.label} className={hint.met ? 'is-met' : ''}>
                    {hint.label}
                  </li>
                ))}
              </ul>
            </div>
          </label>
          <label>
            Sifre (Tekrar)
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              minLength={8}
              maxLength={72}
              required
            />
            {isPasswordMatch ? <span className="field-hint is-ok">Sifreler uyusuyor.</span> : null}
            {isPasswordMismatch ? <span className="field-hint is-bad">Sifreler uyusmuyor.</span> : null}
          </label>
          <label>
            Kisa not (opsiyonel)
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              rows={3}
              maxLength={noteLimit}
            />
            <span className="field-hint note-counter">{note.trim().length}/{noteLimit}</span>
          </label>

          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Gonderiliyor...' : 'Kayit Talebi Gonder'}
          </button>

          {error ? <p className="panel-error">{error}</p> : null}
          {success ? <p className="panel-success">{success}</p> : null}

          <p className="auth-help">
            Hesabin var mi? <Link to="/login">Giris Yap</Link>
          </p>
        </form>
      </div>
    </section>
  )
}
