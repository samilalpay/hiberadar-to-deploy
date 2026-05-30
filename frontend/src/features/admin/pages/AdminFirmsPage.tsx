import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { deleteAdminFirm, listAdminFirms, setAdminFirmActive, type AdminFirmListItem, type PageResponse } from '@/features/panel/api/panel.api'
import { resolveMediaUrl } from '@/shared/lib/media'
import '../../../admin-institutions.css'

export function AdminFirmsPage() {
  const [page, setPage] = useState<PageResponse<AdminFirmListItem> | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [pageIndex, setPageIndex] = useState(0)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'PASSIVE'>('ALL')
  const pageSize = 12

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
      const activeParam = statusFilter === 'ALL' ? undefined : statusFilter === 'ACTIVE'
      const response = await listAdminFirms({ page: pageIndex, size: pageSize, q: searchTerm, active: activeParam })
      setPage(response)
    } catch {
      setError('Firmalar yuklenemedi.')
    } finally {
      setIsLoading(false)
    }
  }

  async function onDelete(id: number) {
    const ok = window.confirm('Firmayi silmek istiyor musunuz?')
    if (!ok) return
    try {
      await deleteAdminFirm(id)
      await load()
    } catch {
      setError('Firma silinemedi. Iliskili kayitlar olabilir.')
    }
  }

  async function onToggleActive(id: number, active: boolean) {
    try {
      await setAdminFirmActive(id, active)
      await load()
    } catch {
      setError('Firma durumu guncellenemedi.')
    }
  }

  useEffect(() => {
    void load()
  }, [pageIndex, searchTerm, statusFilter])

  useEffect(() => {
    setPageIndex(0)
  }, [searchTerm, statusFilter])

  const items = page?.content ?? []

  return (
    <div className="admin-institutions-page">
      <div className="admin-page-header">
        <div>
          <h1>Firmalar</h1>
          <p>Firma profil bilgilerini goruntuleyin ve yonetin.</p>
        </div>
        <div className="admin-firm-actions">
          <input
            className="admin-search-input"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Firma adi veya kullanici ara"
          />
          <button type="button" className="btn" onClick={() => void load()} disabled={isLoading}>
            Yenile
          </button>
        </div>
      </div>

      <div className="panel-toolbar admin-firm-toolbar">
        <div className="panel-chip-row">
          {(['ALL', 'ACTIVE', 'PASSIVE'] as const).map((item) => (
            <button
              key={item}
              type="button"
              className={`btn panel-chip ${statusFilter === item ? 'is-active' : ''}`}
              onClick={() => setStatusFilter(item)}
            >
              {item === 'ALL' ? 'Tumu' : item === 'ACTIVE' ? 'Aktif' : 'Pasif'}
            </button>
          ))}
        </div>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="admin-table-section">
        {isLoading ? (
          <p>Yukleniyor...</p>
        ) : items.length === 0 ? (
          <p>Firma bulunamadi.</p>
        ) : (
          <table className="admin-table">
            <thead>
              <tr>
                <th>Logo</th>
                <th>Firma Adi</th>
                <th>Kullanici</th>
                <th>Profil</th>
                <th>Durum</th>
                <th>Sektor</th>
                <th>Ulke</th>
                <th>Calisan</th>
                <th>Islemler</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td>
                    {item.companyLogoUrl ? (
                      <img
                        src={resolveMediaUrl(item.companyLogoUrl)}
                        alt={item.companyName ?? item.username}
                        className="admin-logo-thumb"
                      />
                    ) : (
                      <span className="admin-logo-placeholder">
                        {(item.companyName ?? item.username).slice(0, 2).toUpperCase()}
                      </span>
                    )}
                  </td>
                  <td>{item.companyName || '-'}</td>
                  <td>{item.username}</td>
                  <td>{item.profileCompleted ? 'Tamam' : 'Eksik'}</td>
                  <td>{item.active ? 'Aktif' : 'Pasif'}</td>
                  <td>{item.sector || '-'}</td>
                  <td>{item.countryCode || '-'}</td>
                  <td>{item.employees ?? '-'}</td>
                  <td className="admin-table-actions">
                    <Link className="btn btn-sm btn-ghost" to={`/admin/firms/${item.id}`}>
                      Detay
                    </Link>
                    <button
                      type="button"
                      className="btn btn-sm btn-ghost"
                      onClick={() => void onToggleActive(item.id, !item.active)}
                    >
                      {item.active ? 'Pasiflestir' : 'Aktiflestir'}
                    </button>
                    <button type="button" className="btn btn-sm btn-ghost btn-danger" onClick={() => void onDelete(item.id)}>
                      Sil
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
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
    </div>
  )
}
