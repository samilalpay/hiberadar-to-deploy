import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAuth } from '@/features/auth/model/use-auth'
import {
  changeMyPassword,
  getMyProfile,
  uploadMyProfileLogo,
  updateMyProfile,
  type UpdateProfilePayload,
} from '@/features/panel/api/panel.api'
import { resolveMediaUrl } from '@/shared/lib/media'

const CUSTOM_OPTION = '__CUSTOM__'
const NACE_REGEX = /^\d{2}(?:\.\d{2}){0,3}$/
const APPLICANT_TYPES = ['SME', 'STARTUP', 'ENTREPRENEUR', 'COOPERATIVE', 'NGO', 'UNIVERSITY', 'MUNICIPALITY', 'PUBLIC_INSTITUTION']
const COUNTRY_CODES = ['TR', 'EU', 'US', 'DE', 'FR', 'GB', 'NL', 'IT', 'ES', 'AE', 'SA', 'QA']
const SECTOR_OPTIONS = ['YAZILIM', 'SAVUNMA', 'SAGLIK', 'URETIM', 'ENERJI', 'TARIM', 'EGITIM', 'LOJISTIK', 'FINTECH', 'BIYOTEKNOLOJI', 'OTOMOTIV', 'SIBER GUVENLIK']
const ACTIVITY_OPTIONS = ['TEKNOLOJI', 'AR-GE', 'IHRACAT', 'DIJITAL DONUSUM', 'URETIM OPTIMIZASYONU', 'YAPAY ZEKA', 'YESEIL DONUSUM', 'VERI ANALITIGI']
const NACE_SUGGESTIONS = ['62.01', '62.02', '62.09', '72.19', '72.11', '26.11', '28.29']
const EMPLOYEE_PRESETS = ['1', '3', '5', '10', '25', '50', '100', '250', '500']
const TURNOVER_PRESETS = ['500000', '1000000', '2500000', '5000000', '10000000', '25000000', '50000000']

function normalizeOptionKey(value: string): string {
  return value
    .trim()
    .toLocaleLowerCase('tr-TR')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/ı/g, 'i')
    .toUpperCase()
}

function findOptionInsensitive(options: string[], value: string): string | undefined {
  const normalized = normalizeOptionKey(value)
  return options.find((option) => normalizeOptionKey(option) === normalized)
}

function parseNumericInput(value: string): number {
  const digits = value.replace(/\D/g, '')
  if (!digits) return 0
  const parsed = Number(digits)
  return Number.isFinite(parsed) ? parsed : 0
}

function formatNumberInput(value: number | undefined): string {
  if (!value || value <= 0) return ''
  return value.toLocaleString('tr-TR')
}

function normalizeNaceCodesInput(value: string): string {
  return value
    .split(/[,;\n]+/)
    .map((code) => code.trim().toUpperCase())
    .filter((code) => code.length > 0)
    .join(', ')
}

function hasInvalidNaceCode(value: string): boolean {
  const tokens = value
    .split(/[,;\n]+/)
    .map((code) => code.trim())
    .filter((code) => code.length > 0)

  return tokens.some((token) => !NACE_REGEX.test(token))
}

const initialForm: UpdateProfilePayload = {
  companyName: '',
  applicantType: 'SME',
  companyAgeMonths: 0,
  employees: 1,
  countryCode: 'TR',
  cofundingAvailable: false,
  cofundingRate: 0,
  sector: '',
  activityArea: '',
  turnover: 0,
  naceCodes: '',
}

export function FirmProfilePage() {
  const { role, username } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const initialTab = searchParams.get('tab') === 'password' ? 'password' : 'profile'
  const [activeTab, setActiveTab] = useState<'profile' | 'password'>(initialTab)
  const [form, setForm] = useState<UpdateProfilePayload>(initialForm)
  const [employeesInput, setEmployeesInput] = useState('')
  const [turnoverInput, setTurnoverInput] = useState('')
  const [selectedSector, setSelectedSector] = useState('YAZILIM')
  const [customSector, setCustomSector] = useState('')
  const [selectedActivity, setSelectedActivity] = useState('TEKNOLOJI')
  const [customActivity, setCustomActivity] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [logoFile, setLogoFile] = useState<File | null>(null)
  const [logoPreview, setLogoPreview] = useState<string | null>(null)
  const [logoUrl, setLogoUrl] = useState<string | null>(null)
  const [logoMessage, setLogoMessage] = useState('')
  const [contactInfo, setContactInfo] = useState({
    fullName: '',
    phone: '',
    email: '',
  })
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    newPasswordAgain: '',
  })
  const [passwordMessage, setPasswordMessage] = useState('')
  const [passwordError, setPasswordError] = useState('')
  const [lastPasswordChange, setLastPasswordChange] = useState<string | null>(null)

  useEffect(() => {
    const stored = window.localStorage.getItem('hiberadar:lastPasswordChange')
    if (stored) {
      setLastPasswordChange(stored)
    }
  }, [])

  useEffect(() => {
    const nextTab = searchParams.get('tab') === 'password' ? 'password' : 'profile'
    setActiveTab(nextTab)
  }, [searchParams])

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError('')
      try {
        const me = await getMyProfile()
        const fullName = [me.firstName, me.lastName].filter(Boolean).join(' ').trim()
        setForm({
          companyName: me.companyName ?? '',
          applicantType: me.applicantType ?? 'SME',
          companyAgeMonths: me.companyAgeMonths ?? 0,
          employees: me.employees ?? 1,
          countryCode: me.countryCode ?? 'TR',
          cofundingAvailable: me.cofundingAvailable ?? false,
          cofundingRate: me.cofundingRate ?? 0,
          sector: me.sector ?? '',
          activityArea: me.activityArea ?? '',
          turnover: Number(me.turnover ?? 0),
          naceCodes: me.naceCodes ?? '',
        })
        setLogoUrl(me.companyLogoUrl ?? null)
        setContactInfo({
          fullName,
          phone: me.phone ?? '',
          email: me.email ?? '',
        })
        setEmployeesInput(formatNumberInput(me.employees))
        setTurnoverInput(formatNumberInput(Number(me.turnover ?? 0)))

        const sectorValue = (me.sector ?? '').trim()
        const matchedSector = sectorValue ? findOptionInsensitive(SECTOR_OPTIONS, sectorValue) : undefined
        if (matchedSector) {
          setSelectedSector(matchedSector)
          setCustomSector('')
        } else {
          setSelectedSector(CUSTOM_OPTION)
          setCustomSector(me.sector ?? '')
        }

        const activityValue = (me.activityArea ?? '').trim()
        const matchedActivity = activityValue ? findOptionInsensitive(ACTIVITY_OPTIONS, activityValue) : undefined
        if (matchedActivity) {
          setSelectedActivity(matchedActivity)
          setCustomActivity('')
        } else {
          setSelectedActivity(CUSTOM_OPTION)
          setCustomActivity(me.activityArea ?? '')
        }
      } catch {
        setError('Profil bilgisi yuklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }
    void load()
  }, [])

  useEffect(() => {
    if (!logoFile) {
      setLogoPreview(null)
      return
    }
    const previewUrl = URL.createObjectURL(logoFile)
    setLogoPreview(previewUrl)
    return () => {
      URL.revokeObjectURL(previewUrl)
    }
  }, [logoFile])

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')
    setError('')

    if (!form.companyName.trim()) {
      setError('Lutfen firma adini yazin.')
      return
    }

    if (selectedSector === CUSTOM_OPTION && !customSector.trim()) {
      setError('Lutfen sektor alanini doldurun.')
      return
    }
    if (selectedActivity === CUSTOM_OPTION && !customActivity.trim()) {
      setError('Lutfen faaliyet alani alanini doldurun.')
      return
    }

    if (form.naceCodes.trim() && hasInvalidNaceCode(form.naceCodes.trim())) {
      setError('NACE formati gecersiz. Ornek: 62.01 veya 62.01, 72.19')
      return
    }

    try {
      const normalizedNaceCodes = normalizeNaceCodesInput(form.naceCodes)
      const payload: UpdateProfilePayload = {
        ...form,
        applicantType: form.applicantType.trim().toUpperCase(),
        countryCode: form.countryCode.trim().toUpperCase(),
        sector: selectedSector === CUSTOM_OPTION ? customSector.trim() : selectedSector,
        activityArea: selectedActivity === CUSTOM_OPTION ? customActivity.trim() : selectedActivity,
        naceCodes: normalizedNaceCodes,
      }
      await updateMyProfile(payload)
      setMessage('Profiliniz guncellendi.')
    } catch {
      setError('Profil guncellenemedi. Alanlari kontrol edin.')
    }
  }

  async function onUploadLogo() {
    if (!logoFile) {
      setLogoMessage('Lutfen bir logo dosyasi secin.')
      return
    }
    setLogoMessage('')
    setError('')
    try {
      const response = await uploadMyProfileLogo(logoFile)
      setLogoUrl(response.companyLogoUrl ?? null)
      setLogoFile(null)
      setLogoMessage('Logo guncellendi.')
      window.dispatchEvent(new Event('profile:changed'))
    } catch {
      setError('Logo yuklenemedi. Dosyayi kontrol edin.')
    }
  }

  async function onPasswordSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPasswordMessage('')
    setPasswordError('')

    if (passwordForm.newPassword.length < 8) {
      setPasswordError('Yeni sifre en az 8 karakter olmali.')
      return
    }

    if (passwordForm.newPassword !== passwordForm.newPasswordAgain) {
      setPasswordError('Yeni sifre ve tekrar sifresi ayni olmali.')
      return
    }

    try {
      const response = await changeMyPassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      })
      setPasswordMessage(response.message || 'Sifreniz guncellendi.')
      setPasswordForm({ currentPassword: '', newPassword: '', newPasswordAgain: '' })
      const updatedAt = new Date().toISOString()
      window.localStorage.setItem('hiberadar:lastPasswordChange', updatedAt)
      setLastPasswordChange(updatedAt)
    } catch (err) {
      const backendMessage =
        typeof err === 'object' && err && 'response' in err
          ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined
      setPasswordError(backendMessage || 'Sifre guncellenemedi.')
    }
  }

  const pageTitle = role === 'FIRMA' ? 'Firma Profili' : 'Kullanici Profili'
  const pageDescription = useMemo(
    () =>
      role === 'FIRMA'
        ? 'Kurumsal profilinizi guncelleyip hibe eslesmelerinizi guclendirin.'
        : 'Hesap bilgilerinizi ve sifre guvenliginizi tek ekrandan yonetin.',
    [role],
  )

  function activateTab(tab: 'profile' | 'password') {
    setActiveTab(tab)
    setSearchParams({ tab })
  }

  function onEmployeesChange(value: string) {
    const numeric = parseNumericInput(value)
    setEmployeesInput(formatNumberInput(numeric))
    setForm((prev) => ({ ...prev, employees: numeric }))
  }

  function onTurnoverChange(value: string) {
    const numeric = parseNumericInput(value)
    setTurnoverInput(formatNumberInput(numeric))
    setForm((prev) => ({ ...prev, turnover: numeric }))
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

  const strength = getPasswordStrength(passwordForm.newPassword)
  const passwordHints = [
    { label: 'En az 8 karakter', met: passwordForm.newPassword.length >= 8 },
    { label: 'En az 1 buyuk harf', met: /[A-Z]/.test(passwordForm.newPassword) },
    { label: 'En az 1 kucuk harf', met: /[a-z]/.test(passwordForm.newPassword) },
    { label: 'En az 1 sayi', met: /[0-9]/.test(passwordForm.newPassword) },
    { label: 'En az 1 sembol', met: /[^A-Za-z0-9]/.test(passwordForm.newPassword) },
  ]

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>{pageTitle}</h1>
        <p>{pageDescription}</p>
      </div>

      <article className="page-card profile-hero-card">
        <div className="profile-hero-details">
          <h3>{username ?? 'Kullanici'} hesabiniz</h3>
          <p>Hesabinizda profil ayarlari ve sifre guvenligi islemlerini yapabilirsiniz.</p>
          {contactInfo.fullName || contactInfo.phone || contactInfo.email ? (
            <p>
              {contactInfo.fullName || 'Iletisim bilgisi yok'}
              {contactInfo.phone ? ` · ${contactInfo.phone}` : ''}
              {contactInfo.email ? ` · ${contactInfo.email}` : ''}
            </p>
          ) : null}
        </div>
        <div className="panel-chip-row">
          <button
            type="button"
            className={`btn panel-chip ${activeTab === 'profile' ? 'is-active' : ''}`}
            onClick={() => activateTab('profile')}
          >
            Profil Bilgileri
          </button>
          <button
            type="button"
            className={`btn panel-chip ${activeTab === 'password' ? 'is-active' : ''}`}
            onClick={() => activateTab('password')}
          >
            Sifre Guncelle
          </button>
        </div>
      </article>

      {activeTab === 'profile' ? (
      <>
      <article className="page-card profile-logo-card">
        <div className="profile-logo-row">
          <div className="profile-logo-preview">
            {logoPreview || logoUrl ? (
              <img
                src={logoPreview ?? resolveMediaUrl(logoUrl)}
                alt="Firma logosu"
              />
            ) : (
              <span>Logo</span>
            )}
          </div>
          <div className="profile-logo-actions">
            <h4>Firma Logosu</h4>
            <p>Profilinizde ve panelde gorunur. PNG, JPG, WEBP veya SVG yukleyin.</p>
            <input
              type="file"
              accept="image/png,image/jpeg,image/webp,image/svg+xml"
              onChange={(e) => setLogoFile(e.target.files?.[0] ?? null)}
            />
            <button type="button" className="btn" onClick={() => void onUploadLogo()} disabled={isLoading}>
              Logo Yukle
            </button>
            {logoMessage ? <small className="auth-help">{logoMessage}</small> : null}
          </div>
        </div>
      </article>
      <form className="panel-form-grid profile-modern-form" onSubmit={onSubmit}>
        <label>
          Firma Adi
          <input
            value={form.companyName}
            onChange={(e) => setForm((p) => ({ ...p, companyName: e.target.value }))}
            placeholder="Orn: HibeRadar Teknoloji"
          />
        </label>
        <label>
          Basvuru Tipi
          <select value={form.applicantType} onChange={(e) => setForm((p) => ({ ...p, applicantType: e.target.value }))}>
            {APPLICANT_TYPES.map((type) => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </label>
        <label>
          Sirket Yasi (Ay)
          <input type="number" value={form.companyAgeMonths} onChange={(e) => setForm((p) => ({ ...p, companyAgeMonths: Number(e.target.value) }))} />
        </label>
        <label>
          Calisan Sayisi
          <input
            type="text"
            inputMode="numeric"
            value={employeesInput}
            onChange={(e) => onEmployeesChange(e.target.value)}
            list="employee-presets"
            placeholder="Orn: 25"
          />
          <datalist id="employee-presets">
            {EMPLOYEE_PRESETS.map((preset) => (
              <option key={preset} value={Number(preset).toLocaleString('tr-TR')} />
            ))}
          </datalist>
        </label>
        <label>
          Ulke Kodu
          <select value={form.countryCode} onChange={(e) => setForm((p) => ({ ...p, countryCode: e.target.value }))}>
            {COUNTRY_CODES.map((country) => (
              <option key={country} value={country}>{country}</option>
            ))}
          </select>
        </label>
        <label>
          Sektor
          <select
            value={selectedSector}
            onChange={(e) => {
              const value = e.target.value
              setSelectedSector(value)
              if (value !== CUSTOM_OPTION) {
                setForm((p) => ({ ...p, sector: value }))
              }
            }}
          >
            {SECTOR_OPTIONS.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
            <option value={CUSTOM_OPTION}>DIGER</option>
          </select>
          {selectedSector === CUSTOM_OPTION ? (
            <input
              placeholder="Sektor girin"
              value={customSector}
              onChange={(e) => {
                setCustomSector(e.target.value)
                setForm((p) => ({ ...p, sector: e.target.value }))
              }}
            />
          ) : null}
        </label>
        <label>
          Faaliyet Alani
          <select
            value={selectedActivity}
            onChange={(e) => {
              const value = e.target.value
              setSelectedActivity(value)
              if (value !== CUSTOM_OPTION) {
                setForm((p) => ({ ...p, activityArea: value }))
              }
            }}
          >
            {ACTIVITY_OPTIONS.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
            <option value={CUSTOM_OPTION}>DIGER</option>
          </select>
          {selectedActivity === CUSTOM_OPTION ? (
            <input
              placeholder="Faaliyet alani girin"
              value={customActivity}
              onChange={(e) => {
                setCustomActivity(e.target.value)
                setForm((p) => ({ ...p, activityArea: e.target.value }))
              }}
            />
          ) : null}
        </label>
        <label>
          Ciro
          <input
            type="text"
            inputMode="numeric"
            value={turnoverInput}
            onChange={(e) => onTurnoverChange(e.target.value)}
            list="turnover-presets"
            placeholder="Orn: 1.000.000"
          />
          <datalist id="turnover-presets">
            {TURNOVER_PRESETS.map((preset) => (
              <option key={preset} value={Number(preset).toLocaleString('tr-TR')} />
            ))}
          </datalist>
          <small className="auth-help">Yazi yazarken otomatik binlik ayirac uygulanir (orn: 1.000.000).</small>
        </label>
        <label>
          NACE Kodlari
          <input
            value={form.naceCodes}
            onChange={(e) => setForm((p) => ({ ...p, naceCodes: e.target.value }))}
            list="nace-suggestions"
            placeholder="Orn: 62.01 veya 62.01, 62.02"
          />
          <datalist id="nace-suggestions">
            {NACE_SUGGESTIONS.map((code) => (
              <option key={code} value={code} />
            ))}
          </datalist>
          <small className="auth-help">Birden fazla NACE icin virgul kullanin. Diger kodlari da serbestce yazabilirsiniz.</small>
        </label>
        <label>
          Es Finansman Orani
          <input type="number" value={form.cofundingRate} onChange={(e) => setForm((p) => ({ ...p, cofundingRate: Number(e.target.value) }))} />
        </label>
        <label className="panel-checkbox">
          <input
            type="checkbox"
            checked={form.cofundingAvailable}
            onChange={(e) => setForm((p) => ({ ...p, cofundingAvailable: e.target.checked }))}
          />
          Es finansman saglayabilirim
        </label>
        <button type="submit" className="btn btn-primary" disabled={isLoading}>Kaydet</button>
      </form>
      </>
      ) : (
      <div className="profile-security-stack">
        <aside className="page-card profile-security-tips">
          <div className="profile-tips-head">
            <span className="security-icon" aria-hidden="true" />
            <div>
              <h4>Guvenlik Onerileri</h4>
              <p>Kurumsal politika icin en az 8 karakter ve karmasik sifre tercih edin.</p>
            </div>
          </div>
          <ul>
            <li>Buyuk-kucuk harf, sayi ve sembol kombinasyonu kullanin.</li>
            <li>Firma e-posta sifresiyle ayni sifreyi kullanmayin.</li>
            <li>Degisiklikten sonra paneli yeniden acmaniz gerekmez.</li>
          </ul>
          <div className="profile-tips-meta">
            <span>Son Guncelleme:</span>
            <strong>{lastPasswordChange ? new Date(lastPasswordChange).toLocaleDateString('tr-TR') : 'Bilgi yok'}</strong>
          </div>
        </aside>

        <form className="panel-form-grid profile-modern-form profile-password-form" onSubmit={onPasswordSubmit}>
          <div className="profile-security-head">
            <h3>Sifre Guncelle</h3>
            <p>Mevcut sifrenizi dogrulayin ve yeni sifre belirleyin.</p>
          </div>
          <label>
            Mevcut Sifre
            <input
              type="password"
              value={passwordForm.currentPassword}
              onChange={(e) => setPasswordForm((prev) => ({ ...prev, currentPassword: e.target.value }))}
              placeholder="Mevcut sifrenizi girin"
            />
          </label>
          <label>
            Yeni Sifre
            <input
              type="password"
              value={passwordForm.newPassword}
              onChange={(e) => setPasswordForm((prev) => ({ ...prev, newPassword: e.target.value }))}
              placeholder="En az 8 karakter"
            />
            <div className="password-strength-row">
              <span className={`strength-label is-${strength.className}`}>
                Sifre guclulugu: {strength.label}
              </span>
              <div className={`strength-meter is-${strength.className}`}>
                <span style={{ width: `${Math.min(100, (strength.score / 6) * 100)}%` }} />
              </div>
              <ul className="password-hint-list">
                {passwordHints.map((hint) => (
                  <li key={hint.label} className={hint.met ? 'is-met' : ''}>{hint.label}</li>
                ))}
              </ul>
            </div>
          </label>
          <label>
            Yeni Sifre Tekrar
            <input
              type="password"
              value={passwordForm.newPasswordAgain}
              onChange={(e) => setPasswordForm((prev) => ({ ...prev, newPasswordAgain: e.target.value }))}
              placeholder="Yeni sifreyi tekrar girin"
            />
          </label>
          <button type="submit" className="btn btn-primary" disabled={isLoading}>Sifreyi Guncelle</button>
        </form>
      </div>
      )}

      {activeTab === 'profile' ? (
        <div className="profile-feedback">
          {message ? <p className="panel-success">{message}</p> : null}
          {error ? <p className="panel-error">{error}</p> : null}
        </div>
      ) : (
        <div className="profile-feedback">
          {passwordMessage ? <p className="panel-success">{passwordMessage}</p> : null}
          {passwordError ? <p className="panel-error">{passwordError}</p> : null}
        </div>
      )}
    </section>
  )
}
