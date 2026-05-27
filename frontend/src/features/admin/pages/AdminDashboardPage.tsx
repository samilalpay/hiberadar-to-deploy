import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { listAdminApplications, listAdminFirmRegistrations, listAdminGrants } from '@/features/panel/api/panel.api'

export function AdminDashboardPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [kpis, setKpis] = useState({
    pendingFirms: 0,
    publishedGrants: 0,
    totalGrants: 0,
    submittedApplications: 0,
  })

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError('')
      try {
        const [pendingFirms, published, total, submittedApps] = await Promise.all([
          listAdminFirmRegistrations({ status: 'PENDING', page: 0, size: 1 }),
          listAdminGrants({ status: 'PUBLISHED', page: 0, size: 1 }),
          listAdminGrants({ page: 0, size: 1 }),
          listAdminApplications({ status: 'SUBMITTED', page: 0, size: 1 }),
        ])
        setKpis({
          pendingFirms: pendingFirms.totalElements,
          publishedGrants: published.totalElements,
          totalGrants: total.totalElements,
          submittedApplications: submittedApps.totalElements,
        })
      } catch {
        setError('Dashboard verileri yuklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [])

  return (
    <section className="admin-dashboard">
      <div className="admin-hero page-card">
        <div>
          <h1>Admin Ana Sayfa</h1>
          <p>Gune baslamak icin kritik metrikler ve hizli islemler bu panelde toplandi.</p>
        </div>
        <div className="admin-hero-actions">
          <Link className="btn btn-primary" to="/admin/grants">Yeni Hibe Ekle</Link>
          <Link className="btn" to="/admin/firm-registrations">Kayit Incele</Link>
        </div>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="admin-kpi-grid">
        <article className="page-card admin-kpi-card">
          <span className="admin-kpi-label">Bekleyen Firma Kaydi</span>
          <strong className="admin-kpi-value">{isLoading ? '-' : kpis.pendingFirms}</strong>
          <p>Onay bekleyen basvurular</p>
        </article>
        <article className="page-card admin-kpi-card">
          <span className="admin-kpi-label">Aktif Hibe</span>
          <strong className="admin-kpi-value">{isLoading ? '-' : kpis.publishedGrants}</strong>
          <p>Yayindaki hibe sayisi</p>
        </article>
        <article className="page-card admin-kpi-card">
          <span className="admin-kpi-label">Yeni Basvuru</span>
          <strong className="admin-kpi-value">{isLoading ? '-' : kpis.submittedApplications}</strong>
          <p>Bekleyen basvuru sayisi</p>
        </article>
        <article className="page-card admin-kpi-card">
          <span className="admin-kpi-label">Toplam Hibe</span>
          <strong className="admin-kpi-value">{isLoading ? '-' : kpis.totalGrants}</strong>
          <p>Yayindaki + taslak + kapali</p>
        </article>
      </div>

      <div className="admin-panels-grid">
        <article className="page-card admin-list-card">
          <h2>Hizli Islem</h2>
          <div className="admin-quick-actions">
            <Link className="btn" to="/admin/grants">Hibe Yonetimi</Link>
            <Link className="btn" to="/admin/applications">Basvurular</Link>
            <Link className="btn" to="/admin/institutions">Kurumlar</Link>
            <Link className="btn" to="/admin/pre-analysis">On Analiz</Link>
            <Link className="btn" to="/admin/sources">Kaynaklar</Link>
          </div>
        </article>

        <article className="page-card admin-list-card">
          <h2>Operasyon Notlari</h2>
          <ul className="admin-note-list">
            <li>Bugun 3 hibe kaydinda son tarih guncellemesi bekleniyor.</li>
            <li>Ingest kaynaklarinda 1 hatali baglanti var, manuel kontrol onerilir.</li>
            <li>Profili eksik 6 firma icin bilgilendirme e-postasi planlandi.</li>
          </ul>
        </article>
      </div>
    </section>
  )
}
