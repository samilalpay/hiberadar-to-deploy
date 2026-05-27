import { useEffect, useState } from 'react'
import {
  getMyUnreadNotificationCount,
  listMyNotifications,
  markNotificationAsRead,
  type NotificationItem,
} from '@/features/firm/api/notifications.api'

export function AdminNotificationsPage() {
  const [items, setItems] = useState<NotificationItem[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    setIsLoading(true)
    setError('')
    try {
      const [list, unread] = await Promise.all([
        listMyNotifications(0, 30),
        getMyUnreadNotificationCount(),
      ])
      setItems(list.content)
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
  }, [])

  async function onMarkRead(id: number) {
    try {
      await markNotificationAsRead(id)
      setItems((prev) => prev.map((item) => (item.id === id ? { ...item, read: true } : item)))
      setUnreadCount((prev) => Math.max(0, prev - 1))
      window.dispatchEvent(new Event('notifications:changed'))
    } catch {
      setError('Bildirim okundu olarak isaretlenemedi.')
    }
  }

  const unreadItems = items.filter((item) => !item.read)
  const readItems = items.filter((item) => item.read)

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Bildirimler</h1>
        <p>Okunmamis bildirim: <strong>{unreadCount}</strong></p>
      </div>

      <div className="panel-chip-row">
        <button type="button" className="btn" onClick={() => void load()} disabled={isLoading}>Yenile</button>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}
      {isLoading ? <p>Yukleniyor...</p> : null}

      <div className="notification-board-grid">
        <section className="page-card notification-column">
          <div className="notification-column-head">
            <h3>Yeni Bildirimler</h3>
            <span className="notification-count-badge">{unreadItems.length}</span>
          </div>
          {!isLoading && unreadItems.length === 0 ? <p>Yeni bildirim yok.</p> : null}
          <div className="notification-list">
            {unreadItems.map((item) => (
              <article key={item.id} className="notification-card is-unread">
                <h4>{item.title}</h4>
                <p>{item.message}</p>
                <small>{item.createdAt ? new Date(item.createdAt).toLocaleString('tr-TR') : '-'}</small>
                <div className="panel-chip-row">
                  <span className="btn panel-chip is-active">Yeni</span>
                  <button type="button" className="btn btn-primary" onClick={() => void onMarkRead(item.id)}>
                    Okundu Isaretle
                  </button>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="page-card notification-column">
          <div className="notification-column-head">
            <h3>Okunanlar</h3>
            <span className="notification-count-badge is-muted">{readItems.length}</span>
          </div>
          {!isLoading && readItems.length === 0 ? <p>Okunmus bildirim yok.</p> : null}
          <div className="notification-list">
            {readItems.map((item) => (
              <article key={item.id} className="notification-card is-read">
                <h4>{item.title}</h4>
                <p>{item.message}</p>
                <small>{item.createdAt ? new Date(item.createdAt).toLocaleString('tr-TR') : '-'}</small>
                <div className="panel-chip-row">
                  <span className="btn panel-chip">Okundu</span>
                </div>
              </article>
            ))}
          </div>
        </section>
      </div>
    </section>
  )
}
