import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listGrants, type GrantItem } from '@/features/firm/api/grants.api'
import { useInstitutionsQuery } from '@/features/admin/api/institutions.api'
import type { Institution } from '@/shared/types/institution'
import { resolveMediaUrl } from '@/shared/lib/media'

type TabFilter = 'ALL' | 'PUBLISHED' | 'CLOSED'
type QuickFilter = 'ALL' | 'URGENT_ONLY' | 'APPROACHING'
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

function getInstitutionBrand(item: GrantItem, institutions: Institution[]): InstitutionBrand {
  const provider = item.providerName ?? ''
  const fallback = [item.programName, item.title].filter(Boolean).join(' ')

  const exact = institutions.find((i) => i.name === provider)
  if (exact) return { shortCode: exact.shortCode, logoSrc: resolveMediaUrl(exact.logoUrl) }

  const normProvider = normalizeForSearch(provider)
  const normFallback = normalizeForSearch(fallback)

  const fuzzy = institutions.find((i) => {
    const normName = normalizeForSearch(i.name)
    if (!normName) return false
    return normProvider.includes(normName) || normFallback.includes(normName)
  })

  if (fuzzy) return { shortCode: fuzzy.shortCode, logoSrc: resolveMediaUrl(fuzzy.logoUrl) }

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

function getCardSummary(item: GrantItem): string {
  if (item.summaryShort?.trim()) return item.summaryShort.trim()
  const provider = item.providerName?.trim() || 'Kurum'
  const program = item.programName?.trim() || item.title?.trim() || 'destek programi'
  return `${provider} tarafindan sunulan ${program} cagrisi.`
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

function formatDate(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString('tr-TR')
}

export function FirmGrantsPage() {
  const [items, setItems] = useState<GrantItem[]>([])
  const [tab, setTab] = useState<TabFilter>('ALL')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [totals, setTotals] = useState({ published: 0, closed: 0, total: 0 })
  const [quickFilter, setQuickFilter] = useState<QuickFilter>('ALL')
  const { data: institutions = [] } = useInstitutionsQuery()

  async function fetchAllByStatus(status: 'PUBLISHED' | 'CLOSED') {
    const PAGE_SIZE = 100
    let pageIndex = 0
    let accumulated: GrantItem[] = []
    let total = 0

    while (true) {
      const page = await listGrants({ status, page: pageIndex, size: PAGE_SIZE })
      if (pageIndex === 0) {
        total = page.totalElements
      }
      accumulated = [...accumulated, ...page.content]
      if (accumulated.length >= total || page.content.length < PAGE_SIZE) {
        break
      }
      pageIndex += 1
    }

    return { items: accumulated, total }
  }

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError('')
      try {
        const [publishedMeta, closedMeta] = await Promise.all([
          listGrants({ status: 'PUBLISHED', page: 0, size: 1 }),
          listGrants({ status: 'CLOSED', page: 0, size: 1 }),
        ])
        const nextTotals = {
          published: publishedMeta.totalElements,
          closed: closedMeta.totalElements,
          total: publishedMeta.totalElements + closedMeta.totalElements,
        }
        setTotals(nextTotals)

        if (tab === 'ALL') {
          const [published, closed] = await Promise.all([
            fetchAllByStatus('PUBLISHED'),
            fetchAllByStatus('CLOSED'),
          ])
          setItems([...published.items, ...closed.items])
        } else {
          const data = await fetchAllByStatus(tab)
          setItems(data.items)
        }
      } catch {
        setError('Hibeler yuklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [tab])

  const sortedItems = [...items].sort((a, b) => {
    const statusRank: Record<string, number> = {
      PUBLISHED: 0,
      CLOSED: 1,
      DRAFT: 2,
    }
    const aRank = statusRank[a.status ?? 'PUBLISHED'] ?? 9
    const bRank = statusRank[b.status ?? 'PUBLISHED'] ?? 9
    if (aRank !== bRank) return aRank - bRank

    const aDate = a.deadlineAt ? new Date(a.deadlineAt).getTime() : Number.POSITIVE_INFINITY
    const bDate = b.deadlineAt ? new Date(b.deadlineAt).getTime() : Number.POSITIVE_INFINITY
    if (aDate !== bDate) return aDate - bDate

    return (a.title ?? '').localeCompare(b.title ?? '', 'tr-TR')
  })

  const visibleItems = sortedItems.filter((item) => {
    const urgency = getUrgency(item.deadlineAt, item.status)
    if (quickFilter === 'URGENT_ONLY') return urgency.label === 'Acil'
    if (quickFilter === 'APPROACHING') return urgency.label === 'Yaklasiyor'
    return true
  })

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Firma Hibeleri</h1>
        <p>Kullanicinin hibeleri gezmesi icin sade liste paneli.</p>
      </div>

      <div className="grant-filter-row">
        <div className="panel-chip-row">
          <button
            type="button"
            className={`btn panel-chip ${tab === 'ALL' ? 'is-active' : ''}`}
            onClick={() => setTab('ALL')}
          >
            Tum Hibeler
          </button>
          <button
            type="button"
            className={`btn panel-chip ${tab === 'PUBLISHED' ? 'is-active' : ''}`}
            onClick={() => setTab('PUBLISHED')}
          >
            Acik Cagrilar
          </button>
          <button
            type="button"
            className={`btn panel-chip ${tab === 'CLOSED' ? 'is-active' : ''}`}
            onClick={() => setTab('CLOSED')}
          >
            Kapali Cagrilar
          </button>
        </div>

        <div className="quick-filter-row">
          <button
            type="button"
            className={`btn ${quickFilter === 'URGENT_ONLY' ? 'btn-primary' : ''}`}
            onClick={() => setQuickFilter((prev) => (prev === 'URGENT_ONLY' ? 'ALL' : 'URGENT_ONLY'))}
          >
            Sadece Acil
          </button>
          <button
            type="button"
            className={`btn ${quickFilter === 'APPROACHING' ? 'btn-primary' : ''}`}
            onClick={() => setQuickFilter((prev) => (prev === 'APPROACHING' ? 'ALL' : 'APPROACHING'))}
          >
            Yaklasanlar
          </button>
        </div>
      </div>

      <div className="panel-stats-grid">
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-total" aria-hidden="true" />
          <span>Toplam Hibe</span>
          <strong>{totals.total}</strong>
        </div>
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-open" aria-hidden="true" />
          <span>Yayindaki</span>
          <strong>{totals.published}</strong>
        </div>
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-closed" aria-hidden="true" />
          <span>Kapali</span>
          <strong>{totals.closed}</strong>
        </div>
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-listed" aria-hidden="true" />
          <span>Listelenen</span>
          <strong>{visibleItems.length}</strong>
        </div>
      </div>

      {error ? <p>{error}</p> : null}

      <div className="inline-actions" style={{ flexDirection: 'column' }}>
        {!isLoading && visibleItems.length === 0 ? <p>Bu filtrede gosterilecek hibe bulunmuyor.</p> : null}

        {visibleItems.map((item) => {
          const institutionBrand = getInstitutionBrand(item, institutions)
          const urgency = getUrgency(item.deadlineAt, item.status)
          return (
            <article key={item.id} className={`page-card grant-card-mini ${item.status === 'CLOSED' ? 'grant-card-frozen' : ''}`}>
              <div className="grant-card-mini-left">
                <div className="grant-card-title-row">
                  {institutionBrand.logoSrc ? (
                    <img className="institution-logo" src={institutionBrand.logoSrc} alt={`${item.providerName ?? 'Kurum'} logosu`} />
                  ) : (
                    <span className="institution-logo-fallback" aria-hidden="true">{institutionBrand.shortCode}</span>
                  )}
                  <h3>{(item.providerName ?? 'Kurum')} - {(item.programName ?? item.title)}</h3>
                </div>
                <p className="grant-card-meta-line"><span className={`urgency-pill ${urgency.className}`}>{urgency.label}</span> {urgency.remainingText}</p>
                <p className="grant-card-subline">Baslangic: {formatDate(item.publishedAt)} | Bitis: {formatDate(item.deadlineAt)}</p>
                <p className="grant-card-summary"><strong>Ozet:</strong> {getCardSummary(item)}</p>
              </div>
              <div className="inline-actions grant-card-mini-actions">
                <span className={`grant-status-dot ${item.status === 'CLOSED' ? 'grant-status-dot-closed' : 'grant-status-dot-open'}`} />
                <Link className="btn btn-primary" to={`/app/grants/${item.id}`}>Detay</Link>
              </div>
            </article>
          )
        })}
      </div>
    </section>
  )
}
