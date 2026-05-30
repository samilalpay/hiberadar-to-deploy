import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  listAdminFirmRegistrations,
  setAdminFirmRegistrationStatus,
  type AdminFirmRegistration,
  type PageResponse,
} from '@/features/panel/api/panel.api'

export function AdminFirmRegistrationsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const initialStatusParam = searchParams.get('status')
  const initialStatus: 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' =
    initialStatusParam === 'APPROVED' || initialStatusParam === 'REJECTED' || initialStatusParam === 'PENDING'
      ? initialStatusParam
      : 'PENDING'
  const [status, setStatus] = useState<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>(initialStatus)
  const [page, setPage] = useState<PageResponse<AdminFirmRegistration> | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState<string>('')
  const [counts, setCounts] = useState({ all: 0, pending: 0, approved: 0, rejected: 0 })
  const [pageIndex, setPageIndex] = useState(0)
  const [sort, setSort] = useState<'createdAt,desc' | 'createdAt,asc'>('createdAt,desc')
  const [decisionNotes, setDecisionNotes] = useState<Record<number, string>>({})

  const statusLabels = useMemo(
    () => ({
      ALL: 'Tumu',
      PENDING: 'Bekleyen',
      APPROVED: 'Onaylanan',
      REJECTED: 'Reddedilen',
    }),
    [],
  )

  const pageWindow = useMemo(() => {
    const total = page?.totalPages ?? 0
    if (total <= 1) return []
    const maxButtons = 5
    const half = Math.floor(maxButtons / 2)
    let start = Math.max(0, pageIndex - half)
    let end = Math.min(total - 1, start + maxButtons - 1)
    if (end - start < maxButtons - 1) {
      start = Math.max(0, end - maxButtons + 1)
    }
    return Array.from({ length: end - start + 1 }, (_, idx) => start + idx)
  }, [page?.totalPages, pageIndex])

  async function load() {
    setIsLoading(true)
    setError('')
    try {
      const [list, allCount, pendingCount, approvedCount, rejectedCount] = await Promise.all([
        listAdminFirmRegistrations({
          status: status === 'ALL' ? undefined : status,
          page: pageIndex,
          size: 12,
          sort,
        }),
        listAdminFirmRegistrations({ size: 1 }),
        listAdminFirmRegistrations({ status: 'PENDING', size: 1 }),
        listAdminFirmRegistrations({ status: 'APPROVED', size: 1 }),
        listAdminFirmRegistrations({ status: 'REJECTED', size: 1 }),
      ])

      setPage(list)
      setCounts({
        all: allCount.totalElements,
        pending: pendingCount.totalElements,
        approved: approvedCount.totalElements,
        rejected: rejectedCount.totalElements,
      })
      setLastUpdated(new Date().toLocaleString('tr-TR'))
    } catch {
      setError('Kayit talepleri yuklenemedi.')
    } finally {
      setIsLoading(false)
    }
  }

  async function updateStatus(id: number, next: 'APPROVED' | 'REJECTED') {
    try {
      await setAdminFirmRegistrationStatus(id, next, decisionNotes[id])
      setPage((current) => {
        if (!current) return current
        const updated = current.content
          .map((item) =>
            item.id === id
              ? {
                  ...item,
                  status: next,
                  decidedAt: new Date().toISOString(),
                  decisionNote: decisionNotes[id]?.trim() || item.decisionNote,
                }
              : item,
          )
          .filter((item) => status !== 'PENDING' || item.status === 'PENDING')
        return { ...current, content: updated }
      })
      setDecisionNotes((current) => {
        const nextNotes = { ...current }
        delete nextNotes[id]
        return nextNotes
      })
      await load()
    } catch {
      setError('Kayit durumu guncellenemedi.')
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status, pageIndex, sort])

  useEffect(() => {
    setPageIndex(0)
  }, [status, sort])

  useEffect(() => {
    setSearchParams((current) => {
      const next = new URLSearchParams(current)
      if (status === 'ALL') {
        next.delete('status')
      } else {
        next.set('status', status)
      }
      return next
    })
  }, [setSearchParams, status])

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <div>
          <h1>Firma Kayit Talepleri</h1>
          <p>Bekleyen talepleri degerlendirip karar verin.</p>
        </div>
        <div className="admin-registration-meta-row">
          <span>Son guncelleme: {lastUpdated || '-'}</span>
          <button type="button" className="btn" onClick={() => void load()} disabled={isLoading}>
            Yenile
          </button>
        </div>
      </div>

      <div className="admin-registration-summary">
        <div className="summary-card">
          <span className="summary-label">Toplam</span>
          <strong>{counts.all}</strong>
        </div>
        <div className="summary-card">
          <span className="summary-label">Bekleyen</span>
          <strong>{counts.pending}</strong>
        </div>
        <div className="summary-card">
          <span className="summary-label">Onaylanan</span>
          <strong>{counts.approved}</strong>
        </div>
        <div className="summary-card">
          <span className="summary-label">Reddedilen</span>
          <strong>{counts.rejected}</strong>
        </div>
      </div>

      <div className="panel-toolbar admin-registration-toolbar">
        <div className="panel-chip-row">
          {(['ALL', 'PENDING', 'APPROVED', 'REJECTED'] as const).map((item) => (
            <button
              key={item}
              type="button"
              className={`btn panel-chip ${status === item ? 'is-active' : ''}`}
              onClick={() => setStatus(item)}
            >
              <span>{statusLabels[item]}</span>
              <span className="panel-chip-count">
                {item === 'ALL'
                  ? counts.all
                  : item === 'PENDING'
                    ? counts.pending
                    : item === 'APPROVED'
                      ? counts.approved
                      : counts.rejected}
              </span>
            </button>
          ))}
        </div>
        <div className="admin-registration-sort">
          <label htmlFor="firm-reg-sort">Siralama</label>
          <select
            id="firm-reg-sort"
            value={sort}
            onChange={(event) => setSort(event.target.value as 'createdAt,desc' | 'createdAt,asc')}
          >
            <option value="createdAt,desc">En yeni</option>
            <option value="createdAt,asc">En eski</option>
          </select>
        </div>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="admin-registrations-grid">
        {(page?.content ?? []).map((item) => (
          <article key={item.id} className="admin-registration-card">
            <div className="admin-registration-head">
              <div className="admin-registration-title">
                <h3 title={item.username}>{item.username}</h3>
                <p className="admin-registration-email">
                  <span>E-posta</span>
                  <a href={`mailto:${item.email}`}>{item.email}</a>
                </p>
                {(item.firstName || item.lastName || item.phone) ? (
                  <div className="admin-registration-person">
                    <span className="admin-person-tag">Yetkili</span>
                    <span className="admin-person-item">
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z" />
                      </svg>
                      {`${item.firstName ?? ''} ${item.lastName ?? ''}`.trim() || '-'}
                    </span>
                    <span className="admin-person-item">
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M6.62 10.79a15.05 15.05 0 0 0 6.59 6.59l2.2-2.2a1 1 0 0 1 1.02-.24 11.36 11.36 0 0 0 3.57.57 1 1 0 0 1 1 1V20a1 1 0 0 1-1 1 16 16 0 0 1-14-14 1 1 0 0 1 1-1h2.5a1 1 0 0 1 1 1 11.36 11.36 0 0 0 .57 3.57 1 1 0 0 1-.24 1.02Z" />
                      </svg>
                      {item.phone ?? '-'}
                    </span>
                  </div>
                ) : null}
              </div>
              <span className={`admin-status-pill is-${item.status.toLowerCase()}`}>
                {statusLabels[item.status]}
              </span>
            </div>

            <div className="admin-registration-meta">
              <span>Talep: {item.createdAt ? new Date(item.createdAt).toLocaleString('tr-TR') : '-'}</span>
              <span>Karar: {item.decidedAt ? new Date(item.decidedAt).toLocaleString('tr-TR') : '-'}</span>
            </div>

            <div className="admin-registration-notes">
              <div>
                <p className="note-title">Firma Notu</p>
                <p className="note-body">{item.note?.trim() || 'Not yok.'}</p>
              </div>
              {item.decisionNote ? (
                <div>
                  <p className="note-title">Karar Notu</p>
                  <p className="note-body">{item.decisionNote}</p>
                </div>
              ) : null}
            </div>

            {item.status === 'PENDING' ? (
              <div className="admin-registration-actions">
                <label className="admin-decision-label" htmlFor={`decision-note-${item.id}`}>
                  Karar Notu (istege bagli)
                </label>
                <textarea
                  id={`decision-note-${item.id}`}
                  rows={3}
                  placeholder="Onay/Reddetme gerekcesi"
                  value={decisionNotes[item.id] ?? ''}
                  onChange={(event) =>
                    setDecisionNotes((current) => ({
                      ...current,
                      [item.id]: event.target.value,
                    }))
                  }
                />
                <div className="panel-chip-row">
                  <button type="button" className="btn btn-primary" onClick={() => void updateStatus(item.id, 'APPROVED')}>
                    Onayla
                  </button>
                  <button type="button" className="btn" onClick={() => void updateStatus(item.id, 'REJECTED')}>
                    Reddet
                  </button>
                </div>
              </div>
            ) : null}
          </article>
        ))}
      </div>

      {page && page.totalPages > 1 ? (
        <div className="panel-pagination">
          <button
            type="button"
            className="btn"
            onClick={() => setPageIndex((current) => Math.max(0, current - 1))}
            disabled={isLoading || pageIndex === 0}
          >
            Onceki
          </button>
          <div className="panel-page-buttons">
            {pageWindow.map((pageNumber) => (
              <button
                key={pageNumber}
                type="button"
                className={`btn panel-page-btn ${pageNumber === pageIndex ? 'is-active' : ''}`}
                onClick={() => setPageIndex(pageNumber)}
                disabled={isLoading}
              >
                {pageNumber + 1}
              </button>
            ))}
          </div>
          <button
            type="button"
            className="btn"
            onClick={() => setPageIndex((current) => Math.min((page?.totalPages ?? 1) - 1, current + 1))}
            disabled={isLoading || pageIndex >= (page?.totalPages ?? 1) - 1}
          >
            Sonraki
          </button>
        </div>
      ) : null}

      {!isLoading && (page?.content ?? []).length === 0 ? (
        <div className="panel-empty">
          <h3>Bu filtrede kayit yok.</h3>
          <p>Filtreyi degistirip diger talepleri gorebilirsiniz.</p>
        </div>
      ) : null}
    </section>
  )
}
