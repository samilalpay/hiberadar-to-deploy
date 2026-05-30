import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getAdminFirm, type AdminFirmDetail } from '@/features/panel/api/panel.api'
import { resolveMediaUrl } from '@/shared/lib/media'

export function AdminFirmDetailPage() {
  const { id } = useParams()
  const [firm, setFirm] = useState<AdminFirmDetail | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      if (!id) return
      setIsLoading(true)
      setError('')
      try {
        const response = await getAdminFirm(Number(id))
        setFirm(response)
      } catch {
        setError('Firma bilgileri yuklenemedi.')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [id])

  if (isLoading) {
    return <p>Yukleniyor...</p>
  }

  if (error) {
    return <p className="panel-error">{error}</p>
  }

  if (!firm) {
    return null
  }

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head admin-firm-detail-head">
        <div>
          <h1>Firma Profili</h1>
        </div>
        <Link to="/admin/firms" className="btn">
          Geri Don
        </Link>
      </div>

      <article className="page-card profile-hero-card">
        <div className="profile-logo-row">
          <div className="profile-logo-preview">
            {firm.companyLogoUrl ? (
              <img src={resolveMediaUrl(firm.companyLogoUrl)} alt={firm.companyName ?? firm.username} />
            ) : (
              <span>Logo</span>
            )}
          </div>
          <div className="profile-hero-details">
            <h3>{firm.companyName || 'Firma'}</h3>
            <p>{firm.username} · {firm.email}</p>
            {(firm.firstName || firm.lastName || firm.phone) ? (
              <p>
                {[firm.firstName, firm.lastName].filter(Boolean).join(' ') || 'Iletisim bilgisi yok'}
                {firm.phone ? ` · ${firm.phone}` : ''}
              </p>
            ) : null}
          </div>
        </div>
      </article>

      <div className="panel-form-grid profile-modern-form">
        <div>
          <strong>Profil Durumu</strong>
          <p>{firm.profileCompleted ? 'Tamam' : 'Eksik'}</p>
        </div>
        <div>
          <strong>Iletisim Kisi</strong>
          <p>{[firm.firstName, firm.lastName].filter(Boolean).join(' ') || '-'}</p>
        </div>
        <div>
          <strong>Telefon</strong>
          <p>{firm.phone || '-'}</p>
        </div>
        <div>
          <strong>Hesap Durumu</strong>
          <p>{firm.active ? 'Aktif' : 'Pasif'}</p>
        </div>
        <div>
          <strong>Basvuru Tipi</strong>
          <p>{firm.applicantType || '-'}</p>
        </div>
        <div>
          <strong>Sirket Yasi (Ay)</strong>
          <p>{firm.companyAgeMonths ?? '-'}</p>
        </div>
        <div>
          <strong>Calisan</strong>
          <p>{firm.employees ?? '-'}</p>
        </div>
        <div>
          <strong>Ulke</strong>
          <p>{firm.countryCode || '-'}</p>
        </div>
        <div>
          <strong>Sektor</strong>
          <p>{firm.sector || '-'}</p>
        </div>
        <div>
          <strong>Faaliyet Alani</strong>
          <p>{firm.activityArea || '-'}</p>
        </div>
        <div>
          <strong>Ciro</strong>
          <p>{firm.turnover ? Number(firm.turnover).toLocaleString('tr-TR') : '-'}</p>
        </div>
        <div>
          <strong>Es Finansman</strong>
          <p>{firm.cofundingAvailable ? 'Var' : 'Yok'}</p>
        </div>
        <div>
          <strong>Es Finansman Orani</strong>
          <p>{firm.cofundingRate ?? '-'}</p>
        </div>
        <div>
          <strong>NACE Kodlari</strong>
          <p>{firm.naceCodes || '-'}</p>
        </div>
      </div>
    </section>
  )
}
