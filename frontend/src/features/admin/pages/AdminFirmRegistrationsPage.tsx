import { useEffect, useState } from 'react'
import {
  listAdminFirmRegistrations,
  setAdminFirmRegistrationStatus,
  type AdminFirmRegistration,
  type PageResponse,
} from '@/features/panel/api/panel.api'

export function AdminFirmRegistrationsPage() {
  const [status, setStatus] = useState<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>('PENDING')
  const [page, setPage] = useState<PageResponse<AdminFirmRegistration> | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    setIsLoading(true)
    setError('')
    try {
      const data = await listAdminFirmRegistrations({
        status: status === 'ALL' ? undefined : status,
        size: 12,
      })
      setPage(data)
    } catch {
      setError('Kayit talepleri yuklenemedi.')
    } finally {
      setIsLoading(false)
    }
  }

  async function updateStatus(id: number, next: 'APPROVED' | 'REJECTED') {
    try {
      await setAdminFirmRegistrationStatus(id, next)
      await load()
    } catch {
      setError('Kayit durumu guncellenemedi.')
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status])

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Firma Kayit Talepleri</h1>
        <p>Bekleyen talepleri degerlendirin ve karar verin.</p>
      </div>

      <div className="panel-toolbar">
        <div className="panel-chip-row">
          {(['ALL', 'PENDING', 'APPROVED', 'REJECTED'] as const).map((item) => (
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
        <button type="button" className="btn" onClick={() => void load()} disabled={isLoading}>
          Yenile
        </button>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="panel-list-grid">
        {(page?.content ?? []).map((item) => (
          <article key={item.id} className="page-card panel-list-card">
            <h3>{item.username}</h3>
            <p>{item.email}</p>
            <p>Durum: <strong>{item.status}</strong></p>
            <p>Tarih: {item.createdAt ? new Date(item.createdAt).toLocaleString('tr-TR') : '-'}</p>
            {item.status === 'PENDING' ? (
              <div className="panel-chip-row">
                <button type="button" className="btn btn-primary" onClick={() => void updateStatus(item.id, 'APPROVED')}>
                  Onayla
                </button>
                <button type="button" className="btn" onClick={() => void updateStatus(item.id, 'REJECTED')}>
                  Reddet
                </button>
              </div>
            ) : null}
          </article>
        ))}
      </div>
    </section>
  )
}
