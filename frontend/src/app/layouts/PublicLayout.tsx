import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { Footer } from '@/shared/ui/Footer'

type PublicRouteMeta = { title: string; description: string }

const publicRouteMeta: Record<string, PublicRouteMeta> = {
  '/': {
    title: 'Ana Sayfa',
    description: 'Tesvik robotu ve hibe havuzuna tek noktadan ulasin.',
  },
  '/robot': {
    title: 'Tesvik Robotu',
    description: 'Kisa sorularla firmaniza uygun destekleri hizlica kesfedin.',
  },
  '/grants': {
    title: 'Hibeler',
    description: 'Acil, yaklasan, rahat ve kapali hibeler. Duruma gore filtrele ve basvuru yap.',
  },
  '/login': {
    title: 'Giris',
    description: 'Panelinize erismek icin hesabiniza giris yapin.',
  },
  '/register': {
    title: 'Kayit Ol',
    description: 'HibeRadar platformuna yeni bir hesap olusturun.',
  },
}

function PublicIcon({ kind }: { kind: 'robot' | 'grants' | 'login' | 'register' }) {
  if (kind === 'robot') {
    return <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path d="M9 2h6v2h-2v2h3a4 4 0 014 4v7a5 5 0 01-5 5H9a5 5 0 01-5-5v-7a4 4 0 014-4h3V4H9zm0 8a1.5 1.5 0 100 3 1.5 1.5 0 000-3zm6 0a1.5 1.5 0 100 3 1.5 1.5 0 000-3z" fill="currentColor"/></svg>
  }
  if (kind === 'grants') {
    return <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path d="M12 2l3 6 6 .9-4.5 4.4 1.1 6.2L12 16.8 6.4 19.5l1.1-6.2L3 8.9 9 8z" fill="currentColor"/></svg>
  }
  if (kind === 'login') {
    return <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path d="M10 3h9v18h-9v-2h7V5h-7zM13 12l-4 4v-3H2v-2h7V8z" fill="currentColor"/></svg>
  }
  return <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path d="M15 14c2.7 0 6 1.3 6 3v3H3v-3c0-1.7 3.3-3 6-3h6zm-3-2a4 4 0 100-8 4 4 0 000 8z" fill="currentColor"/></svg>
}

export function PublicLayout() {
  const location = useLocation()
  const pageMeta = publicRouteMeta[location.pathname] ?? publicRouteMeta['/']

  return (
    <div className="public-layout">
      <header className="public-topbar">
        <Link to="/" className="brand">HibeRadar</Link>
        <nav className="public-nav">
          <NavLink to="/robot" className={({ isActive }) => `public-nav-link ${isActive ? 'is-active' : ''}`}><PublicIcon kind="robot" /> Tesvik Robotu</NavLink>
          <NavLink to="/grants" className={({ isActive }) => `public-nav-link ${isActive ? 'is-active' : ''}`}><PublicIcon kind="grants" /> Hibeler</NavLink>
          <NavLink to="/login" className={({ isActive }) => `public-nav-link ${isActive ? 'is-active' : ''}`}><PublicIcon kind="login" /> Giris</NavLink>
          <NavLink to="/register" className={({ isActive }) => `public-nav-link ${isActive ? 'is-active' : ''}`}><PublicIcon kind="register" /> Kayit Ol</NavLink>
        </nav>
        <nav className="public-actions">
          <NavLink to="/robot" className={({ isActive }) => `btn ${isActive ? 'is-active' : ''}`}>Robotu Dene</NavLink>
          <NavLink to="/grants" className={({ isActive }) => `btn btn-primary ${isActive ? 'is-active' : ''}`}>Hibeleri Tara</NavLink>
        </nav>
      </header>

      <main className="public-content">
        <section className="public-context-card" aria-label="Sayfa bilgisi">
          <p className="public-breadcrumbs">HibeRadar / {pageMeta.title}</p>
          <h1>{pageMeta.title}</h1>
          <p>{pageMeta.description}</p>
        </section>
        <Outlet />
      </main>

      <Footer />
    </div>
  )
}
