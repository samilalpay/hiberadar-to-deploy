import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { listGrants, type GrantItem } from '@/features/public/api/public.api'

function formatDate(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString('tr-TR')
}

function formatMoney(value?: number, currency = 'TRY'): string {
  if (value == null || !Number.isFinite(value)) return '-'
  return new Intl.NumberFormat('tr-TR', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value)
}

function getUrgency(deadlineAt?: string): { label: string; className: string; remainingText: string } {
  if (!deadlineAt) {
    return { label: 'Rahat', className: 'urgency-relaxed', remainingText: 'Son tarih belirtilmemis' }
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineAt)
  deadline.setHours(0, 0, 0, 0)

  if (Number.isNaN(deadline.getTime())) {
    return { label: 'Rahat', className: 'urgency-relaxed', remainingText: 'Tarih bilgisi guncel degil' }
  }

  const diffDays = Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return { label: 'Suresi Dolmus', className: 'urgency-expired', remainingText: `${Math.abs(diffDays)} gun once kapandi` }
  }
  if (diffDays <= 7) {
    return { label: 'Acil', className: 'urgency-urgent', remainingText: `${diffDays} gun kaldi` }
  }
  if (diffDays <= 21) {
    return { label: 'Yaklasiyor', className: 'urgency-soon', remainingText: `${diffDays} gun kaldi` }
  }
  return { label: 'Rahat', className: 'urgency-relaxed', remainingText: `${diffDays} gun kaldi` }
}

function getRemainingDays(deadlineAt?: string): number | null {
  if (!deadlineAt) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineAt)
  deadline.setHours(0, 0, 0, 0)
  if (Number.isNaN(deadline.getTime())) return null
  return Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
}

function getSortDaysValue(item: GrantItem): number {
  const remaining = getRemainingDays(item.deadlineAt)
  if (remaining == null) return Number.MAX_SAFE_INTEGER - 2
  if (remaining < 0) return Number.MAX_SAFE_INTEGER - 1
  return remaining
}

function getShortCode(item: GrantItem): string {
  const raw = item.providerName ?? item.programName ?? item.title ?? 'HBR'
  return raw
    .split(/\s+/)
    .filter((part) => part.length > 0)
    .slice(0, 3)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('') || 'HBR'
}

async function fetchAllPublishedGrants() {
  const PAGE_SIZE = 100
  let pageIndex = 0
  let total = 0
  let items: GrantItem[] = []

  while (true) {
    const page = await listGrants({ status: 'PUBLISHED', page: pageIndex, size: PAGE_SIZE })
    if (pageIndex === 0) {
      total = page.totalElements
    }
    items = [...items, ...page.content]
    if (items.length >= total || page.content.length < PAGE_SIZE) {
      break
    }
    pageIndex += 1
  }

  return { items, total }
}

export function LandingPage() {
  const [featured, setFeatured] = useState<GrantItem[]>([])
  const [publishedTotal, setPublishedTotal] = useState(0)
  const [urgentTotal, setUrgentTotal] = useState(0)
  const [soonTotal, setSoonTotal] = useState(0)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    async function loadFeatured() {
      setIsLoading(true)
      try {
        const response = await fetchAllPublishedGrants()
        const sorted = [...response.items].sort((a, b) => getSortDaysValue(a) - getSortDaysValue(b))
        setFeatured(sorted.slice(0, 4))
        setPublishedTotal(response.total)
        setUrgentTotal(response.items.filter((item) => getUrgency(item.deadlineAt).label === 'Acil').length)
        setSoonTotal(response.items.filter((item) => getUrgency(item.deadlineAt).label === 'Yaklasiyor').length)
      } catch {
        setFeatured([])
        setPublishedTotal(0)
        setUrgentTotal(0)
        setSoonTotal(0)
      } finally {
        setIsLoading(false)
      }
    }

    void loadFeatured()
  }, [])

  return (
    <section className="landing-shell">
      <article className="landing-hero-modern">
        <div className="landing-hero-content">
          <p className="landing-kicker">Kurumsal Hibe Kesif Platformu</p>
          <h1>Fikirden Fona Destekten Dijital'e</h1>
          <p>Firma profilinize uygun tesvik ve hibe firsatlarini tek panelde bulun, onceliklendirin ve surece hizli baslayin.</p>
          <div className="hero-actions">
            <Link to="/robot" className="btn btn-primary">Tesvik Robotunu Kullan</Link>
            <Link to="/grants" className="btn">Hibeleri Incele</Link>
            <Link to="/register" className="btn">Ucretsiz Basla</Link>
          </div>
        </div>
        <div className="landing-hero-kpis">
          <div className="landing-kpi-box">
            <span>Yayindaki Cagri</span>
            <strong>{publishedTotal}</strong>
          </div>
          <div className="landing-kpi-box is-urgent">
            <span>Acil Son Tarih</span>
            <strong>{urgentTotal}</strong>
          </div>
          <div className="landing-kpi-box is-soon">
            <span>Yaklasan Cagri</span>
            <strong>{soonTotal}</strong>
          </div>
        </div>
      </article>


    </section>
  )
}
