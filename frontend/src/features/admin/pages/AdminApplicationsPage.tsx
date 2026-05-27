import { useEffect, useState } from 'react'
import { isAxiosError } from 'axios'
import {
  listAdminApplications,
  setAdminApplicationStatus,
  type AdminApplication,
  type PageResponse,
} from '@/features/panel/api/panel.api'

export function AdminApplicationsPage() {
  const [status, setStatus] = useState<'ALL' | 'SUBMITTED' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED'>('ALL')
  const [query, setQuery] = useState('')
  const [page, setPage] = useState<PageResponse<AdminApplication> | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    setIsLoading(true)
    setError('')
    try {
      const data = await listAdminApplications({
        status: status === 'ALL' ? undefined : status,
        search: query,
        size: 12,
      })
      setPage(data)
    } catch (err) {
      if (isAxiosError(err)) {
        const code = err.response?.status
        const detail = typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message
        setError(`Basvurular yuklenemedi${code ? ` (HTTP ${code})` : ''}${detail ? `: ${detail}` : ''}.`)
      } else {
        setError('Basvurular yuklenemedi.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  async function updateStatus(id: number, next: 'IN_REVIEW' | 'APPROVED' | 'REJECTED') {
    try {
      await setAdminApplicationStatus(id, next)
      await load()
    } catch {
      setError('Basvuru durumu guncellenemedi.')
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status])

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Basvuru Yonetimi</h1>
        <p>Basvurularin durumunu surece gore yonetin.</p>
      </div>

      <div className="panel-toolbar">
        <div className="panel-chip-row">
          {(['ALL', 'SUBMITTED', 'IN_REVIEW', 'APPROVED', 'REJECTED'] as const).map((item) => (
            <button
              key={item}
              type="button"
              className={`btn panel-chip ${status === item ? 'is-active' : ''}`}
              onClick={() => setStatus(item)}
            >
              {item}
            </button>
          ))}
        </div>
        <div className="panel-inline-form">
          <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Firma veya hibe ara" />
          <button type="button" className="btn btn-primary" onClick={() => void load()} disabled={isLoading}>
            {isLoading ? 'Yukleniyor...' : 'Ara'}
          </button>
        </div>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="panel-list-grid">
        {(page?.content ?? []).map((item) => (
          <article key={item.id} className="page-card panel-list-card">
            <h3>{item.grantTitle ?? `Basvuru #${item.id}`}</h3>
            <p>Durum: <strong>{item.status}</strong></p>
            <p>Basvuru Tarihi: {item.submittedAt ? new Date(item.submittedAt).toLocaleString('tr-TR') : '-'}</p>
            <div className="panel-chip-row">
              {(['IN_REVIEW', 'APPROVED', 'REJECTED'] as const).map((next) => (
                <button
                  key={next}
                  type="button"
                  className="btn"
                  onClick={() => void updateStatus(item.id, next)}
                  disabled={item.status === next}
                >
                  {next}
                </button>
              ))}
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
