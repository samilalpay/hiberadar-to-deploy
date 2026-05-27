import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { type GrantItem } from '@/features/firm/api/grants.api'
import { listMyMatchedGrants } from '@/features/panel/api/panel.api'

function formatDate(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString('tr-TR')
}

function formatFunding(item: GrantItem): string {
  const currency = item.currency ?? 'TRY'
  if (item.fundingMin == null && item.fundingMax == null) return '-'
  if (item.fundingMin != null && item.fundingMax != null) {
    return `${item.fundingMin.toLocaleString('tr-TR')} - ${item.fundingMax.toLocaleString('tr-TR')} ${currency}`
  }
  if (item.fundingMin != null) {
    return `En az ${item.fundingMin.toLocaleString('tr-TR')} ${currency}`
  }
  return `En fazla ${item.fundingMax!.toLocaleString('tr-TR')} ${currency}`
}

export function FirmMatchesPage() {
  const [items, setItems] = useState<GrantItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError('')
      try {
        const page = await listMyMatchedGrants(0, 20)
        setItems(page.content)
      } catch {
        setError('Eslesen hibeler yuklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [])

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Bana Uygun Hibeler</h1>
        <p>Profilinize gore eslesen hibe cagrilari.</p>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}
      {isLoading ? <p>Yukleniyor...</p> : null}

      <div className="firm-match-grid">
        {!isLoading && items.length === 0 ? <p>Su anda eslesen hibe bulunmuyor.</p> : null}

        {items.map((item) => (
          <article key={item.id} className="page-card firm-match-card">
            <div className="firm-match-head">
              <h3>{item.title}</h3>
              <span className="firm-match-score">Uyum %{item.matchScore ?? 0}</span>
            </div>

            <div className="firm-match-meta">
              <p><strong>Kurum:</strong> {item.providerName ?? '-'}</p>
              <p><strong>Program:</strong> {item.programName ?? '-'}</p>
              <p><strong>Destek Butcesi:</strong> {formatFunding(item)}</p>
              <p><strong>Son Tarih:</strong> {formatDate(item.deadlineAt)}</p>
            </div>

            <div className="firm-match-reasons">
              <p><strong>Neden eslesti?</strong></p>
              {item.matchReasons && item.matchReasons.length > 0 ? (
                <ul>
                  {item.matchReasons.map((reason, idx) => (
                    <li key={`${item.id}-reason-${idx}`}>{reason}</li>
                  ))}
                </ul>
              ) : (
                <p className="firm-match-empty-reason">Bu kayit icin aciklama bilgisi gelmedi.</p>
              )}
            </div>

            <div className="panel-chip-row">
              <Link className="btn btn-primary" to={`/app/grants/${item.id}`}>Detay</Link>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
