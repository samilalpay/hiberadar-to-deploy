import { useEffect, useMemo, useState } from 'react'
import {
  deleteNotification,
  getMyUnreadNotificationCount,
  listMyNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
  type NotificationItem,
  type PageResponse,
} from '@/features/firm/api/notifications.api'

export function FirmNotificationsPage() {
  const [page, setPage] = useState<PageResponse<NotificationItem> | null>(null)
  const [unreadCount, setUnreadCount] = useState(0)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState<'ALL' | 'UNREAD' | 'READ'>('ALL')
  const [pageIndex, setPageIndex] = useState(0)
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
      const readParam = filter === 'ALL' ? undefined : filter === 'READ' ? true : false
      const [list, unread] = await Promise.all([
        listMyNotifications(pageIndex, pageSize, readParam),
        getMyUnreadNotificationCount(),
      ])
      setPage(list)
      setUnreadCount(unread)
      window.dispatchEvent(new Event('notifications:changed'))
    } catch {
      setError('Bildirimler yuklenemedi.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [filter, pageIndex])

  useEffect(() => {
    setPageIndex(0)
  }, [filter])

  async function onMarkRead(id: number) {
    try {
      await markNotificationAsRead(id)
      setPage((current) => {
        if (!current) return current
        if (filter === 'UNREAD') {
          return { ...current, content: current.content.filter((item) => item.id !== id) }
        }
        return {
          ...current,
          content: current.content.map((item) => (item.id === id ? { ...item, read: true } : item)),
        }
      })
      setUnreadCount((prev) => Math.max(0, prev - 1))
      window.dispatchEvent(new Event('notifications:changed'))
      void load()
    } catch {
      setError('Bildirim okundu olarak isaretlenemedi.')
    }
  }

  async function onMarkAllRead() {
    try {
      await markAllNotificationsAsRead()
      setUnreadCount(0)
      window.dispatchEvent(new Event('notifications:changed'))
      void load()
    } catch {
      setError('Bildirimler okundu olarak isaretlenemedi.')
    }
  }

  async function onDelete(id: number) {
    const ok = window.confirm('Bildirim silinsin mi?')
    if (!ok) return
    try {
      const target = page?.content.find((item) => item.id === id)
      await deleteNotification(id)
      setPage((current) => {
        if (!current) return current
        return {
          ...current,
          content: current.content.filter((item) => item.id !== id),
        }
      })
      if (target && !target.read) {
        setUnreadCount((prev) => Math.max(0, prev - 1))
      }
      window.dispatchEvent(new Event('notifications:changed'))
      void load()
    } catch {
      setError('Bildirim silinemedi.')
    }
  }

  const items = page?.content ?? []

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head notifications-head">
        <div>
          <h1>Bildirimler</h1>
          <p>Okunmamis bildirim: <strong>{unreadCount}</strong></p>
        </div>
        <button type="button" className="btn" onClick={() => void load()} disabled={isLoading}>Yenile</button>
      </div>

      <div className="notifications-toolbar">
        <div className="panel-chip-row">
          <button
            type="button"
            className={`btn panel-chip ${filter === 'ALL' ? 'is-active' : ''}`}
            onClick={() => setFilter('ALL')}
          >
            Tumu
          </button>
          <button
            type="button"
            className={`btn panel-chip ${filter === 'UNREAD' ? 'is-active' : ''}`}
            onClick={() => setFilter('UNREAD')}
          >
            Okunmayanlar
            <span className="panel-chip-count">{unreadCount}</span>
          </button>
          <button
            type="button"
            className={`btn panel-chip ${filter === 'READ' ? 'is-active' : ''}`}
            onClick={() => setFilter('READ')}
          >
            Okunanlar
          </button>
        </div>
        <div className="notifications-actions">
          <button
            type="button"
            className="btn"
            onClick={() => void onMarkAllRead()}
            disabled={isLoading || unreadCount === 0}
          >
            Tumunu Okundu Yap
          </button>
        </div>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}
      {isLoading ? <p>Yukleniyor...</p> : null}

      <div className="notifications-grid">
        {items.map((item) => (
          <article key={item.id} className={`notification-card-v2 ${item.read ? 'is-read' : 'is-unread'}`}>
            <div className="notification-card-row">
              <div className="notification-card-main">
                <span className={`notification-icon ${item.read ? 'is-read' : 'is-unread'}`} aria-hidden="true">
                  <svg viewBox="0 0 24 24">
                    <path d="M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm6-6V11a6 6 0 1 0-12 0v5l-2 2v1h16v-1Z" />
                  </svg>
                </span>
                <div>
                  <h3>{item.title}</h3>
                  <p>{item.message}</p>
                </div>
              </div>
              <span className={`notification-pill ${item.read ? 'is-read' : 'is-unread'}`}>
                {item.read ? 'Okundu' : 'Yeni'}
              </span>
            </div>
            <div className="notification-meta">
              <span>{item.createdAt ? new Date(item.createdAt).toLocaleString('tr-TR') : '-'}</span>
              <span className="notification-type">{item.type}</span>
            </div>
            <div className="notification-actions">
              {!item.read ? (
                <button type="button" className="btn btn-primary" onClick={() => void onMarkRead(item.id)}>
                  Okundu Isaretle
                </button>
              ) : null}
              <button type="button" className="btn" onClick={() => void onDelete(item.id)}>
                Sil
              </button>
            </div>
          </article>
        ))}
      </div>

      {!isLoading && items.length === 0 ? (
        <div className="panel-empty">
          <h3>Bildirim bulunamadi.</h3>
          <p>Filtreyi degistirip tekrar deneyebilirsiniz.</p>
        </div>
      ) : null}

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
    </section>
  )
}
