import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

export function Footer() {
  const currentYear = new Date().getFullYear()
  const [isExpanded, setIsExpanded] = useState(false)
  const expandedId = 'public-footer-expanded'

  useEffect(() => {
    const stored = localStorage.getItem('hiberadar.footer.expanded.public')
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
            <span className="footer-tagline">Fikirden fona, destekten dijitale</span>
          </div>
          <nav className="footer-compact-links">
            <Link to="/robot">Tesvik Robotu</Link>
            <Link to="/grants">Hibe Havuzu</Link>
            <Link to="/register">Kayit Ol</Link>
          </nav>
          <button
            type="button"
            className="footer-expand-toggle"
            aria-expanded={isExpanded}
            aria-controls={expandedId}
            onClick={() =>
              setIsExpanded((current) => {
                const next = !current
                localStorage.setItem('hiberadar.footer.expanded.public', String(next))
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
              <h4 className="footer-links-title">Urun</h4>
              <ul className="footer-links">
                <li><Link to="/robot">Tesvik Robotu</Link></li>
                <li><Link to="/grants">Hibe Havuzu</Link></li>
                <li><Link to="/register">Kayit Ol</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="footer-links-title">Kurumsal</h4>
              <ul className="footer-links">
                <li><a href="#neden-hiberadar">Neden HibeRadar</a></li>
                <li><a href="#hakkimizda">Hakkimizda</a></li>
                <li><a href="#iletisim">Iletisim</a></li>
              </ul>
            </div>
            <div>
              <h4 className="footer-links-title">Iletisim</h4>
              <div className="footer-contact-info">
                <div className="contact-item">
                  <span className="contact-label">E-posta</span>
                  <a href="mailto:info@hiberadar.com" className="contact-value">
                    info@hiberadar.com
                  </a>
                </div>
                <div className="contact-item">
                  <span className="contact-label">Telefon</span>
                  <a href="tel:+902120000000" className="contact-value">
                    +90 212 000 00 00
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="footer-compact-bottom">
          <span>© {currentYear} HibeRadar Teknoloji.</span>
          <div className="footer-legal">
            <a href="#kvkk">KVKK Aydinlatma</a>
            <a href="#kullanim-kosullari">Kullanim Kosullari</a>
            <a href="#gizlilik">Gizlilik</a>
          </div>
        </div>
      </div>
    </footer>
  )
}
