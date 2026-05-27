import { useAuth } from '@/features/auth/model/use-auth'

export function PanelFooter() {
  const { role } = useAuth()
  const currentYear = new Date().getFullYear()
  
  const isAdmin = role === 'ADMIN'
  const isFirma = role === 'FIRMA'

  return (
    <footer className="app-footer">
      <div className="footer-container">
        {/* Ana Bölüm */}
        <div className="footer-main">
          {/* Brand ve Açıklama */}
          <div className="footer-brand-section">
            <div className="footer-brand">HibeRadar</div>
            <p className="footer-tagline">{isAdmin ? '🔧 Platform Yonetimi' : '💼 Firma Paneli'}</p>
            <p className="footer-description">
              {isAdmin 
                ? 'Platform operasyonlarini ve veri akisini merkezi olarak yonetin.'
                : 'Hibe firsatlarini kesfedip basvurularinizi surdurun.'}
            </p>
          </div>

          {/* Hızlı Linkler - Kısayollar */}
          <div className="footer-links-group">
            <h4 className="footer-links-title">⚡ Hizli Erisim</h4>
            <ul className="footer-links">
              {isAdmin ? (
                <>
                  <li><a href="/admin/dashboard">📊 Dashboard</a></li>
                  <li><a href="/admin/grants">🎁 Hibe Yonetimi</a></li>
                  <li><a href="/admin/applications">📋 Basvurular</a></li>
                </>
              ) : (
                <>
                  <li><a href="/app/dashboard">📊 Dashboard</a></li>
                  <li><a href="/app/grants">🎁 Hibeler</a></li>
                  <li><a href="/app/applications">📋 Basvurularim</a></li>
                </>
              )}
            </ul>
          </div>

          {/* Yardım & Destek */}
          <div className="footer-links-group">
            <h4 className="footer-links-title">❓ Destek</h4>
            <ul className="footer-links">
              <li><a href="#rehber">📖 Kullanıcı Rehberi</a></li>
              <li><a href="#sss">❔ Sık Sorulan Sorular</a></li>
              <li><a href="#iletisim">💬 İletişim</a></li>
            </ul>
          </div>

          {/* İletişim */}
          <div className="footer-contact-section">
            <h4 className="footer-links-title">📞 Iletisim</h4>
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

        {/* Divider */}
        <div className="footer-divider"></div>

        {/* Alt Bölüm */}
        <div className="footer-bottom">
          <div className="footer-copyright">
            <span>© {currentYear} HibeRadar Teknoloji. Panel v1.0</span>
          </div>
          <div className="footer-legal">
            <a href="#kvkk">KVKK</a>
            <a href="#kullanim-kosullari">Koşullar</a>
            <a href="#gizlilik">Gizlilik</a>
          </div>
        </div>
      </div>
    </footer>
  )
}
