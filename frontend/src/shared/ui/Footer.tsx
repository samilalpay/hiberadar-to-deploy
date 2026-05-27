import { Link } from 'react-router-dom'

function FooterIcon({ type }: { type: 'product' | 'corporate' | 'mail' | 'phone' }) {
  switch (type) {
    case 'product':
      return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 11h6m-6 4h6M9 7h6"/></svg>
    case 'corporate':
      return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 2v20m-8-5h16M6 7h12v10H6z"/></svg>
    case 'mail':
      return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
    case 'phone':
      return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
  }
}

export function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="app-footer">
      <div className="footer-container">
        {/* Ana Bölüm */}
        <div className="footer-main">
          {/* Brand ve Açıklama */}
          <div className="footer-brand-section">
            <div className="footer-brand">HibeRadar</div>
            <p className="footer-tagline">Fikirden fona, destekten dijitale</p>
            <p className="footer-description">
              Kesif, karsilastirma ve basvuru takibini tek bir panelde toplar.
            </p>
          </div>

          {/* Hızlı Linkler */}
          <div className="footer-links-group">
            <h4 className="footer-links-title">📦 Urun</h4>
            <ul className="footer-links">
              <li><Link to="/robot">🤖 Tesvik Robotu</Link></li>
              <li><Link to="/grants">🎁 Hibe Havuzu</Link></li>
              <li><Link to="/register">✍️ Kayit Ol</Link></li>
            </ul>
          </div>

          {/* Kurumsal */}
          <div className="footer-links-group">
            <h4 className="footer-links-title">🏢 Kurumsal</h4>
            <ul className="footer-links">
              <li><a href="#neden-hiberadar">❓ Neden HibeRadar</a></li>
              <li><a href="#hakkimizda">ℹ️ Hakkimizda</a></li>
              <li><a href="#iletisim">📞 Iletisim</a></li>
            </ul>
          </div>

          {/* İletişim */}
          <div className="footer-contact-section">
            <h4 className="footer-links-title">💬 Iletisim</h4>
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

        {/* Divider */}
        <div className="footer-divider"></div>

        {/* Alt Bölüm */}
        <div className="footer-bottom">
          <div className="footer-copyright">
            <span>© {currentYear} HibeRadar Teknoloji. Tum haklari saklidir.</span>
          </div>
          <div className="footer-legal">
            <a href="#kvkk">KVKK Aydınlatma Metni</a>
            <a href="#kullanim-kosullari">Kullanım Koşulları</a>
            <a href="#gizlilik">Gizlilik Politikası</a>
          </div>
        </div>
      </div>
    </footer>
  )
}
