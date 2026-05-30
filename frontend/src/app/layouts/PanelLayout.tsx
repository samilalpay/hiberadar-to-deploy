import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/model/use-auth'
import { PanelIcon, type IconName } from '@/shared/ui/panel-icons'
import { PanelFooter } from '@/shared/ui/PanelFooter'
import { getMyUnreadNotificationCount } from '@/features/firm/api/notifications.api'
import { getMyProfile } from '@/features/panel/api/panel.api'
import { resolveMediaUrl } from '@/shared/lib/media'

type NavItem = { to: string; label: string; icon: IconName }
type NavGroup = { title: string; links: NavItem[] }

type RouteMeta = { title: string; description: string }

const panelRouteMeta: Record<string, RouteMeta> = {
  '/app/dashboard': {
    title: 'Dashboard',
    description: 'Sirketinize ait genel gorunumu ve son hareketleri takip edin.',
  },
  '/app/robot': {
    title: 'Tesvik Robotu',
    description: 'Firma hesabinizla robotu kullanip sonucu panelde filtreli gorun.',
  },
  '/app/robot/results': {
    title: 'Robot Sonuclari',
    description: 'Misafirdekiyle ayni robot sonuclari firma panelinde listelenir.',
  },
  '/app/grants': {
    title: 'Hibeler',
    description: 'Tum acik hibe ve destek cagrilarini filtreleyerek inceleyin.',
  },
  '/app/grants/matches': {
    title: 'Bana Uygun Hibeler',
    description: 'Firma profilinizle en uyumlu hibe cagrilarini goruntuleyin.',
  },
  '/app/applications': {
    title: 'Basvurularim',
    description: 'Basvuru surecindeki hibe kayitlarinizi tek ekranda yonetin.',
  },
  '/app/meetings': {
    title: 'Randevular',
    description: 'Randevu talebi olusturun, dolu gunleri ve onayli takvimi gorun.',
  },
  '/app/notifications': {
    title: 'Bildirimler',
    description: 'Son tarih, durum ve sistem duyurularini anlik takip edin.',
  },
  '/app/pre-analysis/history': {
    title: 'On Analiz',
    description: 'Gecmis on analiz sonuclarini inceleyip karsilastirin.',
  },
  '/admin/dashboard': {
    title: 'Dashboard',
    description: 'Platform operasyon metriklerini ve oncelikleri takip edin.',
  },
  '/admin/firm-registrations': {
    title: 'Firma Kayitlari',
    description: 'Bekleyen firma kayit basvurularini degerlendirip yonetin.',
  },
  '/admin/grants': {
    title: 'Hibe Yonetimi',
    description: 'Hibe kayitlarini olusturun, yayinlayin ve guncelleyin.',
  },
  '/admin/applications': {
    title: 'Basvurular',
    description: 'Firmalardan gelen basvurulari surec adimlarina gore inceleyin.',
  },
  '/admin/meetings': {
    title: 'Randevu Yonetimi',
    description: 'Randevu slotlarini yonetin, talepleri onaylayin ve takvimi izleyin.',
  },
  '/admin/pre-analysis': {
    title: 'On Analiz',
    description: 'On analiz taleplerini ve sonuclarini operasyonel olarak yonetin.',
  },
  '/admin/ingest': {
    title: 'Veri Akisi',
    description: 'Veri toplama ve isleme akislarinin sagligini takip edin.',
  },
  '/admin/institutions': {
    title: 'Kurumlar Yonetimi',
    description: 'Hibe veren kurumlar, logoları ve bilgilerini yonetin.',
  },
  '/admin/firms': {
    title: 'Firmalar',
    description: 'Firma profillerini ve temel bilgileri goruntuleyin.',
  },
}

function startsWithAny(pathname: string, prefixes: string[]): boolean {
  return prefixes.some((prefix) => pathname.startsWith(prefix))
}

const firmGroups: NavGroup[] = [
  {
    title: 'Calisma Alani',
    links: [
      { to: '/app/dashboard', label: 'Dashboard', icon: 'dashboard' },
      { to: '/app/robot', label: 'Tesvik Robotu', icon: 'robot' },
      { to: '/app/grants', label: 'Hibeler', icon: 'grants' },
      { to: '/app/grants/matches', label: 'Uygun Hibeler', icon: 'analytics' },
    ],
  },
  {
    title: 'Takip',
    links: [
      { to: '/app/applications', label: 'Basvurularim', icon: 'applications' },
      { to: '/app/meetings', label: 'Randevular', icon: 'calendar' },
      { to: '/app/notifications', label: 'Bildirimler', icon: 'alerts' },
      { to: '/app/pre-analysis/history', label: 'On Analiz', icon: 'analytics' },
    ],
  },
  {
    title: 'Hesap',
    links: [
      { to: '/app/profile', label: 'Profil', icon: 'users' },
    ],
  },
]

const adminGroups: NavGroup[] = [
  {
    title: 'Yonetim',
    links: [
      { to: '/admin/dashboard', label: 'Dashboard', icon: 'dashboard' },
      { to: '/admin/firm-registrations', label: 'Firma Kayitlari', icon: 'users' },
      { to: '/admin/grants', label: 'Hibe Yonetimi', icon: 'grants' },
      { to: '/admin/grants/public', label: 'Hibeler', icon: 'grants' },
      { to: '/admin/applications', label: 'Basvurular', icon: 'applications' },
      { to: '/admin/meetings', label: 'Randevu Yonetimi', icon: 'calendar' },
      { to: '/admin/notifications', label: 'Bildirimler', icon: 'alerts' },
    ],
  },
  {
    title: 'Operasyon',
    links: [
      { to: '/admin/firms', label: 'Firmalar', icon: 'users' },
      { to: '/admin/institutions', label: 'Kurumlar', icon: 'institutions' },
      { to: '/admin/pre-analysis', label: 'On Analiz', icon: 'analytics' },
      { to: '/admin/ingest', label: 'Veri Akisi', icon: 'ingest' },
    ],
  },
  {
    title: 'Hesap',
    links: [
      { to: '/admin/profile', label: 'Profil', icon: 'users' },
    ],
  },
]

export function PanelLayout() {
  const { role, username, logout } = useAuth()
  const location = useLocation()
  const groups = role === 'FIRMA' ? firmGroups : adminGroups
  const allLinks = groups.flatMap((group) => group.links)
  const notificationsPath = role === 'FIRMA' ? '/app/notifications' : '/admin/notifications'
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
  const [profileLogoUrl, setProfileLogoUrl] = useState<string | null>(null)
  const [companyName, setCompanyName] = useState<string | null>(null)

  useEffect(() => {
    let disposed = false

    async function loadUnread() {
      try {
        const count = await getMyUnreadNotificationCount()
        if (!disposed) {
          setUnreadCount(count)
        }
      } catch {
        if (!disposed) {
          setUnreadCount(0)
        }
      }
    }

    void loadUnread()
    const timer = window.setInterval(() => {
      void loadUnread()
    }, 20000)

    const onChanged = () => {
      void loadUnread()
    }
    window.addEventListener('notifications:changed', onChanged)

    return () => {
      disposed = true
      window.clearInterval(timer)
      window.removeEventListener('notifications:changed', onChanged)
    }
  }, [role])

  useEffect(() => {
    let disposed = false

    async function loadProfile() {
      try {
        const profile = await getMyProfile()
        if (!disposed) {
          setProfileLogoUrl(profile.companyLogoUrl ?? null)
          setCompanyName(profile.companyName ?? null)
        }
      } catch {
        if (!disposed) {
          setProfileLogoUrl(null)
          setCompanyName(null)
        }
      }
    }

    void loadProfile()
    const onProfileChanged = () => {
      void loadProfile()
    }
    window.addEventListener('profile:changed', onProfileChanged)

    return () => {
      disposed = true
      window.removeEventListener('profile:changed', onProfileChanged)
    }
  }, [username])

  const activeLink = [...allLinks]
    .sort((a, b) => b.to.length - a.to.length)
    .find((link) => location.pathname.startsWith(link.to))
  const pageMeta = (() => {
    const exact = panelRouteMeta[location.pathname]
    if (exact) {
      return exact
    }
    const best = Object.keys(panelRouteMeta)
      .sort((a, b) => b.length - a.length)
      .find((path) => location.pathname.startsWith(path))
    if (best) {
      return panelRouteMeta[best]
    }
    return {
      title: activeLink?.label ?? 'Panel',
      description: role === 'FIRMA'
        ? 'Firma sureclerini yonetmek icin panel modullerini kullanin.'
        : 'Yonetim sureclerini surdurmek icin panel modullerini kullanin.',
    }
  })()

  const isFirmGrantSection = role === 'FIRMA' && startsWithAny(location.pathname, ['/app/grants', '/app/grants/matches'])
  const profileBase = role === 'FIRMA' ? '/app/profile' : '/admin/profile'

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setIsUserMenuOpen(false)
    }, 0)
    return () => window.clearTimeout(timer)
  }, [location.pathname])

  return (
    <div className={`panel-layout ${isSidebarCollapsed ? 'is-collapsed' : ''} ${isSidebarOpen ? 'is-drawer-open' : ''}`}>
      <button
        type="button"
        className="panel-overlay"
        aria-label="Menuyu kapat"
        onClick={() => setIsSidebarOpen(false)}
      />

      <aside className="sidebar">
        <div className="sidebar-brand-row">
          <div className="brand">HibeRadar</div>
          <button
            type="button"
            className="btn sidebar-toggle-btn"
            aria-label="Menuyu daralt"
            onClick={() => setIsSidebarCollapsed((prev) => !prev)}
          >
            {isSidebarCollapsed ? '>' : '<'}
          </button>
        </div>

        <nav className="menu">
          {groups.map((group) => (
            <div key={group.title} className="menu-group">
              <p className="menu-group-title">{group.title}</p>
              {group.links.map((link) => {
                // Parent routes must use exact matching to avoid double-active states.
                // Example: /app/grants should not stay active on /app/grants/matches.
                const shouldMatchExact = allLinks.some((candidate) => candidate.to.startsWith(`${link.to}/`))
                return (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={shouldMatchExact}
                  className={({ isActive }) => `panel-menu-link ${isActive ? 'is-active' : ''}`}
                  onClick={() => setIsSidebarOpen(false)}
                >
                  <span className="panel-link-icon" aria-hidden="true"><PanelIcon name={link.icon} /></span>
                  <span className="panel-link-text">{link.label}</span>
                  {link.icon === 'alerts' && unreadCount > 0 ? (
                    <span className="panel-link-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                  ) : null}
                </NavLink>
                )
              })}
            </div>
          ))}
        </nav>
      </aside>

      <div className="panel-main">
        <header className="panel-topbar">
          <div className="panel-topbar-left">
            <button
              type="button"
              className="btn panel-menu-btn"
              aria-label="Menuyu ac"
              onClick={() => setIsSidebarOpen(true)}
            >
              Menu
            </button>
            <div>
              <strong>{pageMeta.title}</strong>
              <p>{pageMeta.description}</p>
            </div>
          </div>
          <div className="panel-topbar-right">
            {isFirmGrantSection ? (
              <div className="panel-segment-tabs" aria-label="Hibe gorunumu">
                <NavLink
                  to="/app/grants"
                  end
                  className={({ isActive }) => `panel-segment-tab ${isActive ? 'is-active' : ''}`}
                >
                  Hibeler
                </NavLink>
                <NavLink
                  to="/app/grants/matches"
                  className={({ isActive }) => `panel-segment-tab ${isActive ? 'is-active' : ''}`}
                >
                  Bana Uygun
                </NavLink>
              </div>
            ) : null}
            <div className="panel-user-menu">
              <button
                type="button"
                className={`panel-user-badge panel-user-button ${unreadCount > 0 ? 'has-count' : ''}`}
                onClick={() => setIsUserMenuOpen((prev) => !prev)}
              >
                {profileLogoUrl ? (
                  <img
                    className="panel-user-avatar"
                    src={resolveMediaUrl(profileLogoUrl)}
                    alt="Firma logosu"
                  />
                ) : null}
                {companyName ? `${companyName} (${username ?? 'kullanici'})` : (username ?? 'Kullanici')}
                {unreadCount > 0 ? (
                  <span className="panel-user-badge-count">{unreadCount > 99 ? '99+' : unreadCount}</span>
                ) : null}
              </button>
              {isUserMenuOpen ? (
                <div className="panel-user-dropdown" role="menu">
                  <NavLink to={notificationsPath} className="panel-user-dropdown-item" role="menuitem">
                    Bildirimler
                    {unreadCount > 0 ? (
                      <span className="panel-user-dropdown-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                    ) : null}
                  </NavLink>
                  <NavLink to={`${profileBase}?tab=profile`} className="panel-user-dropdown-item" role="menuitem">
                    Profilim
                  </NavLink>
                  <NavLink to={`${profileBase}?tab=password`} className="panel-user-dropdown-item" role="menuitem">
                    Sifremi Guncelle
                  </NavLink>
                </div>
              ) : null}
            </div>
            <button className="btn" onClick={logout}>Cikis</button>
          </div>
        </header>

        <main className="panel-content">
          <Outlet />
        </main>
      </div>

      <PanelFooter />
    </div>
  )
}
