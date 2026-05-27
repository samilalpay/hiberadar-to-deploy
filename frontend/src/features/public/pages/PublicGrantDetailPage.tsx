import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  getGrantDetail,
  getGrantEligibilityRule,
  type GrantDetail,
  type GrantEligibilityRule,
} from '@/features/public/api/public.api'
import { useInstitutionsQuery } from '@/features/admin/api/institutions.api'
import type { Institution } from '@/shared/types/institution'

type Urgency = {
  label: 'Acil' | 'Yaklasiyor' | 'Rahat' | 'Kapali' | 'Suresi Dolmus'
  className: string
  remainingText: string
}

type InstitutionBrand = {
  shortCode: string
  logoSrc?: string | null
}

function normalizeForSearch(value: string): string {
  return value
    .toLocaleLowerCase('tr-TR')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/ı/g, 'i')
    .trim()
}

function getInstitutionBrand(item: GrantDetail, institutions: Institution[]): InstitutionBrand {
  const provider = item.providerName ?? ''
  const fallback = [item.programName, item.title].filter(Boolean).join(' ')

  const exact = institutions.find((i) => i.name === provider)
  if (exact) return { shortCode: exact.shortCode, logoSrc: exact.logoUrl }

  const normProvider = normalizeForSearch(provider)
  const normFallback = normalizeForSearch(fallback)

  const fuzzy = institutions.find((i) => {
    const normName = normalizeForSearch(i.name)
    if (!normName) return false
    return normProvider.includes(normName) || normFallback.includes(normName)
  })

  if (fuzzy) return { shortCode: fuzzy.shortCode, logoSrc: fuzzy.logoUrl }

  const initials = (item.providerName ?? item.programName ?? item.title ?? 'Kurum')
    .split(/\s+/)
    .map((part) => part.trim())
    .filter((part) => part.length > 0)
    .slice(0, 3)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')

  return {
    shortCode: initials || 'KR',
    logoSrc: null,
  }
}

function formatRange(min?: number, max?: number, suffix = ''): string {
  if (min != null && max != null) return `${min.toLocaleString('tr-TR')}${suffix} - ${max.toLocaleString('tr-TR')}${suffix}`
  if (min != null) return `${min.toLocaleString('tr-TR')}${suffix} ve uzeri`
  if (max != null) return `${max.toLocaleString('tr-TR')}${suffix} ve alti`
  return '-'
}

function formatDate(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString('tr-TR')
}

function getUrgency(deadlineAt?: string, status?: string): Urgency {
  if (status === 'CLOSED') {
    return { label: 'Kapali', className: 'urgency-closed', remainingText: 'Cagri kapali' }
  }

  if (!deadlineAt) {
    return { label: 'Rahat', className: 'urgency-relaxed', remainingText: 'Son tarih belirtilmemis' }
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineAt)
  deadline.setHours(0, 0, 0, 0)
  const diffDays = Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return { label: 'Suresi Dolmus', className: 'urgency-expired', remainingText: `${Math.abs(diffDays)} gun once sona erdi` }
  }
  if (diffDays <= 7) {
    return { label: 'Acil', className: 'urgency-urgent', remainingText: `${diffDays} gun kaldi` }
  }
  if (diffDays <= 21) {
    return { label: 'Yaklasiyor', className: 'urgency-soon', remainingText: `${diffDays} gun kaldi` }
  }
  return { label: 'Rahat', className: 'urgency-relaxed', remainingText: `${diffDays} gun kaldi` }
}

export function PublicGrantDetailPage() {
  const params = useParams()
  const grantId = Number(params.id)

  const [item, setItem] = useState<GrantDetail | null>(null)
  const [eligibility, setEligibility] = useState<GrantEligibilityRule | null>(null)
  const [eligibilityMessage, setEligibilityMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const { data: institutions = [] } = useInstitutionsQuery()
  const urgency = item ? getUrgency(item.deadlineAt, item.status) : null
  const institutionBrand = item ? getInstitutionBrand(item, institutions) : null

  useEffect(() => {
    async function load() {
      if (!Number.isFinite(grantId)) {
        setError('Geçersiz hibe kimliği.')
        return
      }

      setIsLoading(true)
      setError('')
      try {
        const [detail, rule] = await Promise.allSettled([
          getGrantDetail(grantId),
          getGrantEligibilityRule(grantId),
        ])

        if (detail.status === 'fulfilled') {
          setItem(detail.value)
        } else {
          setError('Hibe detayı yüklenemedi.')
        }

        if (rule.status === 'fulfilled') {
          setEligibility(rule.value)
          setEligibilityMessage('')
        } else {
          setEligibility(null)
          setEligibilityMessage('Bu çağrı için henüz uygunluk kuralı tanımlanmamış.')
        }
      } catch {
        setError('Hibe detayı yüklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [grantId])

  return (
    <section className="page-card grant-detail-page">
      {isLoading ? <p>Yükleniyor...</p> : null}
      {error ? <p>{error}</p> : null}

      {item ? (
        <>
          <header className="grant-detail-head">
            <div className="grant-detail-brand-row">
              {institutionBrand?.logoSrc ? (
                <img className="institution-logo" src={institutionBrand.logoSrc} alt={`${item.providerName ?? 'Kurum'} logosu`} />
              ) : (
                <span className="institution-logo-fallback" aria-hidden="true">{institutionBrand?.shortCode ?? 'KR'}</span>
              )}
              <div className="grant-detail-brand-text">
                <span className="grant-detail-meta-label">Saglayici</span>
                <strong>{item.providerName ?? '-'}</strong>
              </div>
            </div>
            <h1>{item.title}</h1>
            <span className="grant-detail-meta-label">Cagri Ozeti</span>
            <p className="grant-detail-summary">{item.summaryShort ?? 'Bu cagri icin kisa ozet bilgisi yakinda eklenecek.'}</p>

            <div className="grant-detail-meta-grid">
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Program</span>
                <strong>{item.programName ?? '-'}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Sağlayıcı</span>
                <strong>{item.providerName ?? '-'}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">NACE</span>
                <strong>{item.naceCode ?? '-'}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Ülke</span>
                <strong>{item.countryCode ?? '-'}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Durum</span>
                <strong className={`urgency-pill ${urgency?.className ?? ''}`}>{urgency?.label ?? item.status ?? '-'}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Son Tarihe Kalan</span>
                <strong>{urgency?.remainingText ?? '-'}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Bütçe Aralığı</span>
                <strong>{formatRange(item.fundingMin, item.fundingMax, ' TL')}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Baslangic Tarihi</span>
                <strong>{formatDate(item.publishedAt)}</strong>
              </div>
              <div className="grant-detail-meta-item">
                <span className="grant-detail-meta-label">Bitis Tarihi</span>
                <strong>{formatDate(item.deadlineAt)}</strong>
              </div>
            </div>

            {item.adminQuickInfo ? (
              <div className="grant-detail-note">
                <span className="grant-detail-meta-label">Danisman Notu</span>
                <p>{item.adminQuickInfo}</p>
              </div>
            ) : null}
          </header>

          <section className="page-card grant-detail-eligibility">
            <h2>Uygunluk Kuralları</h2>

            {eligibility ? (
              <div className="grant-detail-meta-grid">
                <div className="grant-detail-meta-item">
                  <span className="grant-detail-meta-label">Başvuru Tipleri</span>
                  <strong>{eligibility.applicantTypes?.length ? eligibility.applicantTypes.join(', ') : '-'}</strong>
                </div>
                <div className="grant-detail-meta-item">
                  <span className="grant-detail-meta-label">Şirket Yaşı</span>
                  <strong>{formatRange(eligibility.minCompanyAgeMonths, undefined, ' ay')}</strong>
                </div>
                <div className="grant-detail-meta-item">
                  <span className="grant-detail-meta-label">Çalışan Aralığı</span>
                  <strong>{formatRange(eligibility.minEmployees, eligibility.maxEmployees)}</strong>
                </div>
                <div className="grant-detail-meta-item">
                  <span className="grant-detail-meta-label">Ciro Aralığı</span>
                  <strong>{formatRange(eligibility.minTurnover, eligibility.maxTurnover, ' TL')}</strong>
                </div>
                <div className="grant-detail-meta-item">
                  <span className="grant-detail-meta-label">Gerekli Ülkeler</span>
                  <strong>{eligibility.requiredCountryCodes?.length ? eligibility.requiredCountryCodes.join(', ') : '-'}</strong>
                </div>
                <div className="grant-detail-meta-item">
                  <span className="grant-detail-meta-label">Eş Finansman</span>
                  <strong>{eligibility.cofundingRequired ? `Gerekli (en az %${eligibility.cofundingRate ?? 0})` : 'Zorunlu değil'}</strong>
                </div>
                {eligibility.notes ? (
                  <div className="grant-detail-meta-item">
                    <span className="grant-detail-meta-label">Basvuru Kosullari Notu</span>
                    <strong>{eligibility.notes}</strong>
                  </div>
                ) : null}
              </div>
            ) : (
              <p>{eligibilityMessage}</p>
            )}
          </section>

          <div className="inline-actions">
            {item.officialUrl ? (
              <a href={item.officialUrl} target="_blank" rel="noreferrer" className="btn">
                Resmi Sayfaya Git
              </a>
            ) : null}
            <Link to="/login" className="btn btn-primary">Uzmanla Görüşme Planla</Link>
            <Link to="/login" className="btn">Danışman Girişi</Link>
          </div>
        </>
      ) : null}
    </section>
  )
}
