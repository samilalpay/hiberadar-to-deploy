export type IconName =
  | 'dashboard'
  | 'users'
  | 'grants'
  | 'robot'
  | 'applications'
  | 'analytics'
  | 'sources'
  | 'ingest'
  | 'institutions'
  | 'calendar'
  | 'alerts'

export function PanelIcon({ name }: { name: IconName }) {
  if (name === 'dashboard') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M3 3h8v8H3zM13 3h8v5h-8zM13 10h8v11h-8zM3 13h8v8H3z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'users') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M16 11c1.66 0 3-1.79 3-4s-1.34-4-3-4-3 1.79-3 4 1.34 4 3 4zM8 11c1.66 0 3-1.79 3-4S9.66 3 8 3 5 4.79 5 7s1.34 4 3 4zm0 2c-2.33 0-7 1.17-7 3.5V20h14v-3.5C15 14.17 10.33 13 8 13zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.94 1.97 3.45V20h7v-3.5c0-2.33-4.67-3.5-7-3.5z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'grants') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M12 2l3 6 6 .9-4.5 4.4 1.1 6.2L12 16.8 6.4 19.5l1.1-6.2L3 8.9 9 8z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'robot') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M9 2h6v2h-2v2h3a4 4 0 014 4v7a5 5 0 01-5 5H9a5 5 0 01-5-5v-7a4 4 0 014-4h3V4H9zm0 8a1.5 1.5 0 100 3 1.5 1.5 0 000-3zm6 0a1.5 1.5 0 100 3 1.5 1.5 0 000-3z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'applications') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M6 2h12v20H6zM9 6h6v2H9zm0 4h6v2H9zm0 4h4v2H9z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'analytics') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M3 20h18v2H3zM6 10h3v8H6zm5-4h3v12h-3zm5 6h3v6h-3z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'sources') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M12 3C7.03 3 3 5.24 3 8s4.03 5 9 5 9-2.24 9-5-4.03-5-9-5zm0 7c-4.42 0-7-1.79-7-2s2.58-2 7-2 7 1.79 7 2-2.58 2-7 2zm-9 4c0 2.76 4.03 5 9 5s9-2.24 9-5v2c0 2.76-4.03 5-9 5s-9-2.24-9-5v-2z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'ingest') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M12 2l4 4h-3v7h-2V6H8zm-7 9h14v9H5zm2 2v5h10v-5z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'institutions') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M20 13H4c-.55 0-1 .45-1 1v6c0 .55.45 1 1 1h16c.55 0 1-.45 1-1v-6c0-.55-.45-1-1-1zM7 19c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zM20 3H4c-.55 0-1 .45-1 1v6c0 .55.45 1 1 1h16c.55 0 1-.45 1-1V4c0-.55-.45-1-1-1zM7 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z" fill="currentColor" />
      </svg>
    )
  }
  if (name === 'calendar') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path d="M7 2h2v2h6V2h2v2h3v18H4V4h3zm11 8H6v10h12V10zM6 8h12V6H6v2z" fill="currentColor" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
      <path d="M12 2a10 10 0 100 20 10 10 0 000-20zm1 14h-2v-2h2zm0-4h-2V7h2z" fill="currentColor" />
    </svg>
  )
}
