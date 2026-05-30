import { useEffect, useState } from 'react'
import { useAuth } from '@/features/auth/model/use-auth'

export function PanelFooter() {
  const { role } = useAuth()
  const currentYear = new Date().getFullYear()
  const [isExpanded, setIsExpanded] = useState(false)
  const expandedId = 'panel-footer-expanded'
  
  const isAdmin = role === 'ADMIN'
  const isFirma = role === 'FIRMA'

  useEffect(() => {
    const stored = localStorage.getItem('hiberadar.footer.expanded.panel')
    if (stored === 'true') {
      setIsExpanded(true)
    }
  }, [])

  return (
    <footer className={`app-footer app-footer-compact ${isExpanded ? 'is-expanded' : ''}`}>
      <div className="footer-container footer-compact">
        <div className="footer-compact-main">
          <div className="footer-compact-brand">
            <span className="footer-brand">HibeRadar</span>
            <span className="footer-tagline">{isAdmin ? 'Platform Yonetimi' : 'Firma Paneli'}</span>
          </div>
          <nav className="footer-compact-links">
            {isAdmin ? (
              <>
                <a href="/admin/dashboard">Dashboard</a>
                <a href="/admin/grants">Hibe Yonetimi</a>
                <a href="/admin/applications">Basvurular</a>
              </>
            ) : (
              <>
                <a href="/app/dashboard">Dashboard</a>
                <a href="/app/grants">Hibeler</a>
                <a href="/app/applications">Basvurularim</a>
              </>
            )}
          </nav>
          <button
            type="button"
            className="footer-expand-toggle"
            aria-expanded={isExpanded}
            aria-controls={expandedId}
            onClick={() =>
              setIsExpanded((current) => {
                const next = !current
                localStorage.setItem('hiberadar.footer.expanded.panel', String(next))
                return next
              })
            }
          >
            {isExpanded ? 'Daha az bilgi' : 'Daha fazla bilgi'}
          </button>
        </div>

        <div id={expandedId} className="footer-expanded" hidden={!isExpanded}>
          <div className="footer-expanded-grid">
            <div>
              <h4 className="footer-links-title">Hizli Erisim</h4>
              <ul className="footer-links">
                {isAdmin ? (
                  <>
                    <li><a href="/admin/dashboard">Dashboard</a></li>
                    <li><a href="/admin/grants">Hibe Yonetimi</a></li>
                    <li><a href="/admin/applications">Basvurular</a></li>
                  </>
                ) : (
                  <>
                    <li><a href="/app/dashboard">Dashboard</a></li>
                    <li><a href="/app/grants">Hibeler</a></li>
                    <li><a href="/app/applications">Basvurularim</a></li>
                  </>
                )}
              </ul>
            </div>
            <div>
              <h4 className="footer-links-title">Destek</h4>
              <ul className="footer-links">
                <li><a href="#rehber">Kullanici Rehberi</a></li>
                <li><a href="#sss">Sik Sorulan Sorular</a></li>
                <li><a href="#iletisim">Iletisim</a></li>
              </ul>
            </div>
            <div>
              <h4 className="footer-links-title">Iletisim</h4>
              <div className="footer-contact-info">
                <div className="contact-item">
                  <span className="contact-label">E-posta</span>
                  <a href="mailto:support@hiberadar.com" className="contact-value">
                    support@hiberadar.com
                  </a>
                </div>
                <div className="contact-item">
                  <span className="contact-label">Destek</span>
                  <a href="tel:+902120000001" className="contact-value">
                    +90 212 000 00 01
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="footer-compact-bottom">
          <span>© {currentYear} HibeRadar Teknoloji. Panel v1.0</span>
          <div className="footer-legal">
            <a href="#kvkk">KVKK</a>
            <a href="#kullanim-kosullari">Kosullar</a>
            <a href="#gizlilik">Gizlilik</a>
          </div>
        </div>
      </div>
    </footer>
  )
}
