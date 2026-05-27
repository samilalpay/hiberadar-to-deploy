import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listGrants, type GrantItem } from '@/features/firm/api/grants.api'
import { getMyProfile, listMyApplications, listMyMatchedGrants } from '@/features/panel/api/panel.api'

export function FirmDashboardPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [openGrantCount, setOpenGrantCount] = useState(0)
  const [matchedCount, setMatchedCount] = useState(0)
  const [myApplicationCount, setMyApplicationCount] = useState(0)
  const [profileReady, setProfileReady] = useState(false)
  const [upcoming, setUpcoming] = useState<GrantItem[]>([])

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError('')
      try {
        const [publishedMeta, publishedPage, matched, mine, profile] = await Promise.all([
          listGrants({ status: 'PUBLISHED', page: 0, size: 1 }),
          listGrants({ status: 'PUBLISHED', page: 0, size: 50 }),
          listMyMatchedGrants(0, 6),
          listMyApplications(0, 1),
          getMyProfile(),
        ])
        setOpenGrantCount(publishedMeta.totalElements)
        setMatchedCount(matched.totalElements)
        setMyApplicationCount(mine.totalElements)
        setProfileReady(profile.profileCompleted)

        const nearest = [...publishedPage.content]
          .filter((item) => item.deadlineAt)
          .sort((a, b) => String(a.deadlineAt).localeCompare(String(b.deadlineAt)))
          .slice(0, 4)
        setUpcoming(nearest)
      } catch {
        setError('Dashboard verileri yuklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [])

  return (
    <section className="panel-page-grid">
      <article className="page-card panel-list-card">
        <h1>Firma Dashboard</h1>
        <p>Guncel durumunuz ve sonraki adimlar tek bakista.</p>
      </article>

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="panel-stats-grid">
        <article className="page-card panel-stat-card">
          <span className="panel-stat-icon is-open" aria-hidden="true" />
          <span>Acik Hibe</span>
          <strong>{isLoading ? '-' : openGrantCount}</strong>
        </article>
        <article className="page-card panel-stat-card">
          <span className="panel-stat-icon is-listed" aria-hidden="true" />
          <span>Bana Uygun Hibe</span>
          <strong>{isLoading ? '-' : matchedCount}</strong>
        </article>
        <article className="page-card panel-stat-card">
          <span className="panel-stat-icon is-total" aria-hidden="true" />
          <span>Basvurularim</span>
          <strong>{isLoading ? '-' : myApplicationCount}</strong>
        </article>
        <article className="page-card panel-stat-card">
          <span className="panel-stat-icon is-status" aria-hidden="true" />
          <span>Profil Durumu</span>
          <strong>{isLoading ? '-' : profileReady ? 'Tamam' : 'Eksik'}</strong>
        </article>
      </div>

      <div className="panel-list-grid">
        <article className="page-card panel-list-card">
          <h3>Yaklasan Son Tarihler</h3>
          <div className="panel-stack">
            {upcoming.map((item) => (
              <div key={item.id} className="panel-row">
                <span>{item.title}</span>
                <small>{item.deadlineAt ? new Date(item.deadlineAt).toLocaleDateString('tr-TR') : '-'}</small>
              </div>
            ))}
          </div>
        </article>

        <article className="page-card panel-list-card">
          <h3>Hizli Islem</h3>
          <div className="panel-chip-row">
            <Link className="btn" to="/app/grants">Hibeleri Incele</Link>
            <Link className="btn" to="/app/grants/matches">Bana Uygun</Link>
            <Link className="btn" to="/app/applications">Basvurularim</Link>
            <Link className="btn" to="/app/profile">Profilim</Link>
          </div>
        </article>
      </div>
    </section>
  )
}
