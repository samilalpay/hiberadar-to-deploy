import { useEffect, useMemo, useState } from 'react'
import { listMyApplications, type AdminApplication } from '@/features/panel/api/panel.api'

export function FirmApplicationsPage() {
  const [items, setItems] = useState<AdminApplication[]>([])
  const [status, setStatus] = useState<'ALL' | 'SUBMITTED' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED'>('ALL')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError('')
      try {
        const data = await listMyApplications(0, 30)
        setItems(data.content)
      } catch {
        setError('Basvurular yuklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }
    void load()
  }, [])

  const visible = useMemo(
    () => (status === 'ALL' ? items : items.filter((item) => item.status === status)),
    [items, status],
  )

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Basvurularim</h1>
        <p>Basvuru surecinizi adim adim takip edin.</p>
      </div>

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

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="panel-list-grid">
        {isLoading ? <p>Yukleniyor...</p> : null}
        {visible.map((item) => (
          <article key={item.id} className="page-card panel-list-card">
            <h3>{item.grantTitle ?? `Basvuru #${item.id}`}</h3>
            <p>Durum: <strong>{item.status}</strong></p>
            <p>Basvuru: {item.submittedAt ? new Date(item.submittedAt).toLocaleString('tr-TR') : '-'}</p>
            <p>Karar: {item.decisionNote ?? '-'}</p>
          </article>
        ))}
      </div>
    </section>
  )
}
