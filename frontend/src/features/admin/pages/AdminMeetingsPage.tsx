import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  listAdminMeetingCalendar,
  listUnavailableMeetingDays,
  confirmMeeting,
  rejectMeeting,
  cancelMeeting,
  type MeetingCalendarItem,
} from '@/features/panel/api/panel.api'
import { MeetingTimeboard, type MeetingTimeboardSlot } from '@/shared/ui/MeetingTimeboard'

function formatDateTime(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('tr-TR')
}

function toDateKey(value: Date): string {
  const y = value.getFullYear()
  const m = String(value.getMonth() + 1).padStart(2, '0')
  const d = String(value.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function buildMonthDays(base: Date): Date[] {
  const first = new Date(base.getFullYear(), base.getMonth(), 1)
  const startWeekday = (first.getDay() + 6) % 7
  const start = new Date(first)
  start.setDate(first.getDate() - startWeekday)

  const days: Date[] = []
  for (let i = 0; i < 42; i += 1) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    days.push(d)
  }
  return days
}

export function AdminMeetingsPage() {
  const [calendarItems, setCalendarItems] = useState<MeetingCalendarItem[]>([])
  const [unavailableDays, setUnavailableDays] = useState<string[]>([])
  const [selectedDayKey, setSelectedDayKey] = useState(() => toDateKey(new Date()))
  const [calendarMonth, setCalendarMonth] = useState(() => new Date())
  const [noteByApp, setNoteByApp] = useState<Record<number, string>>({})
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const load = useCallback(async () => {
    setError('')
    try {
      const [meetings, blockedDays] = await Promise.all([
        listAdminMeetingCalendar(),
        listUnavailableMeetingDays(true),
      ])
      setCalendarItems(meetings)
      setUnavailableDays(blockedDays)
    } catch {
      setError('Randevu verileri yuklenemedi.')
    }
  }, [])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void load()
  }, [load])

  async function onConfirmMeeting(applicationId: number, requestedMeetingAt?: string) {
    if (!requestedMeetingAt) {
      setError('Talep saati bulunamadi.')
      return
    }
    setError('')
    setMessage('')
    try {
      await confirmMeeting(applicationId, requestedMeetingAt, noteByApp[applicationId], 'APPROVE')
      setMessage('Randevu onaylandi ve takvime islendi.')
      await load()
    } catch (err: unknown) {
      const messageFromApi =
        typeof err === 'object' && err && 'response' in err
          ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined
      setError(messageFromApi || 'Randevu onaylanamadi. Saat dolu olabilir.')
    }
  }

  async function onRejectMeeting(applicationId: number, requestedMeetingAt?: string) {
    if (!requestedMeetingAt) {
      setError('Talep saati bulunamadi.')
      return
    }
    setError('')
    setMessage('')
    try {
      await rejectMeeting(applicationId, noteByApp[applicationId])
      setMessage('Randevu talebi reddedildi.')
      await load()
    } catch (err: unknown) {
      const messageFromApi =
        typeof err === 'object' && err && 'response' in err
          ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined
      setError(messageFromApi || 'Randevu talebi reddedilemedi.')
    }
  }

  const requestedItems = useMemo(
    () => calendarItems.filter((item) => item.meetingStatus === 'REQUESTED' && item.requestedMeetingAt && !item.confirmedMeetingAt),
    [calendarItems],
  )
  const confirmedItems = useMemo(
    () => calendarItems.filter((item) => item.meetingStatus === 'CONFIRMED' && item.confirmedMeetingAt),
    [calendarItems],
  )
  const unavailableDaySet = useMemo(() => new Set(unavailableDays), [unavailableDays])
  const meetingDaySet = useMemo(
    () => new Set(calendarItems.map((item) => item.effectiveMeetingAt?.slice(0, 10)).filter(Boolean) as string[]),
    [calendarItems],
  )
  const monthDays = useMemo(() => buildMonthDays(calendarMonth), [calendarMonth])
  const meetingsByDay = useMemo(() => {
    const grouped: Record<
      string,
      Array<{
        time: string
        status: 'REQUESTED' | 'CONFIRMED'
        requestedAt?: string
        decidedAt?: string
        note?: string
        firmUsername?: string
        grantTitle?: string
        applicationId: number
      }>
    > = {}
    for (const item of calendarItems) {
      const when = item.effectiveMeetingAt
      if (!when) continue
      const key = when.slice(0, 10)
      if (!grouped[key]) grouped[key] = []
      grouped[key].push({
        time: new Date(when).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' }),
        status: item.confirmedMeetingAt ? 'CONFIRMED' : 'REQUESTED',
        requestedAt: item.requestedMeetingAt,
        decidedAt: item.decidedAt,
        note: item.meetingNote,
        firmUsername: item.firmUsername,
        grantTitle: item.grantTitle,
        applicationId: item.applicationId,
      })
    }
    Object.values(grouped).forEach((items) => items.sort((a, b) => a.time.localeCompare(b.time)))
    return grouped
  }, [calendarItems])
  const timeOptions = useMemo(() => {
    const options: string[] = []
    for (let hour = 9; hour <= 17; hour += 1) {
      options.push(`${String(hour).padStart(2, '0')}:00`)
    }
    return options
  }, [])

  const selectedDayMeetingsMap = useMemo(() => {
    const map = new Map<
      string,
      {
        status: 'REQUESTED' | 'CONFIRMED'
        requestedAt?: string
        decidedAt?: string
        note?: string
        firmUsername?: string
        grantTitle?: string
        applicationId: number
      }
    >()
    const items = meetingsByDay[selectedDayKey] ?? []
    for (const item of items) {
      map.set(item.time, {
        status: item.status,
        requestedAt: item.requestedAt,
        decidedAt: item.decidedAt,
        note: item.note,
        firmUsername: item.firmUsername,
        grantTitle: item.grantTitle,
        applicationId: item.applicationId,
      })
    }
    return map
  }, [meetingsByDay, selectedDayKey])

  const selectedDaySlots = useMemo<MeetingTimeboardSlot[]>(() => {
    return timeOptions.map((time) => {
      const info = selectedDayMeetingsMap.get(time)
      const state = !info ? 'AVAILABLE' : info.status === 'REQUESTED' ? 'REQUESTED' : 'CONFIRMED'
      const label = !info ? 'Bos' : info.status === 'REQUESTED' ? 'Onay Bekliyor' : 'Onaylandi'
      return {
        key: `detail-${time}`,
        time,
        label,
        state,
        disabled: true,
        tooltipTitle: info?.grantTitle ?? (info ? `Basvuru #${info.applicationId}` : undefined),
        tooltipLines: info
          ? [
              `Firma: ${info.firmUsername ?? '-'}`,
              `Talep: ${formatDateTime(info.requestedAt)}`,
              `Karar Zamani: ${formatDateTime(info.decidedAt)}`,
              `Not: ${info.note?.trim() ? info.note : '-'}`,
            ]
          : undefined,
      }
    })
  }, [selectedDayMeetingsMap, timeOptions])

  async function onCancelMeeting(applicationId: number, confirmedMeetingAt?: string) {
    if (!confirmedMeetingAt) {
      setError('Onayli randevu saati bulunamadi.')
      return
    }
    setError('')
    setMessage('')
    try {
      await cancelMeeting(applicationId, noteByApp[applicationId])
      setMessage('Onayli randevu iptal edildi ve saat tekrar uygun hale geldi.')
      await load()
    } catch (err: unknown) {
      const messageFromApi =
        typeof err === 'object' && err && 'response' in err
          ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined
      setError(messageFromApi || 'Randevu iptal edilemedi.')
    }
  }

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Randevu Yonetimi</h1>
        <p>Slot olusturma yok. Firmalar dogrudan talep aciyor, siz saat onayliyorsunuz.</p>
      </div>

      {message ? <p className="panel-success">{message}</p> : null}
      {error ? <p className="panel-error">{error}</p> : null}

      <article className="page-card meeting-calendar-card">
        <div className="meeting-calendar-head">
          <h3>Aylik Takvim</h3>
          <div className="panel-chip-row">
            <button type="button" className="btn" onClick={() => setCalendarMonth((m) => new Date(m.getFullYear(), m.getMonth() - 1, 1))}>Onceki</button>
            <span className="btn">{calendarMonth.toLocaleDateString('tr-TR', { month: 'long', year: 'numeric' })}</span>
            <button type="button" className="btn" onClick={() => setCalendarMonth((m) => new Date(m.getFullYear(), m.getMonth() + 1, 1))}>Sonraki</button>
          </div>
        </div>

        <div className="meeting-month-grid">
          {['Pzt', 'Sal', 'Car', 'Per', 'Cum', 'Cmt', 'Paz'].map((label) => (
            <strong key={label} className="meeting-weekday">{label}</strong>
          ))}
          {monthDays.map((day) => {
            const key = toDateKey(day)
            const isCurrentMonth = day.getMonth() === calendarMonth.getMonth()
            const isUnavailable = unavailableDaySet.has(key)
            const hasMeeting = meetingDaySet.has(key)
            const dayMeetings = meetingsByDay[key] ?? []
            return (
              <div
                key={key}
                className={`meeting-day-cell ${isCurrentMonth ? '' : 'is-out'} ${isUnavailable ? 'is-unavailable' : 'is-available'} ${hasMeeting ? 'has-meeting' : ''}`}
                onClick={() => setSelectedDayKey(key)}
              >
                <span>{day.getDate()}</span>
                <div className="meeting-day-times">
                  {dayMeetings.map((meeting, idx) => (
                    <span key={`${key}-meeting-${idx}`} className="meeting-time-state-wrap">
                      <span className={`meeting-time-state ${meeting.status === 'REQUESTED' ? 'is-requested' : 'is-confirmed'}`}>
                        {meeting.time} {meeting.status === 'REQUESTED' ? 'Onay Bekliyor' : 'Onaylandi'}
                      </span>
                      <span className="meeting-time-tooltip" role="tooltip">
                        <strong>{meeting.status === 'REQUESTED' ? 'Bekleyen Talep' : 'Onayli Randevu'}</strong>
                        <span>Talep Zamani: {formatDateTime(meeting.requestedAt)}</span>
                        <span>Not: {meeting.note?.trim() ? meeting.note : '-'}</span>
                      </span>
                    </span>
                  ))}
                </div>
              </div>
            )
          })}
        </div>
      </article>

      <article className="page-card meeting-calendar-card">
        <h3>Secili Gun Saat Detayi</h3>
        <p className="meeting-help-text">{new Date(`${selectedDayKey}T00:00:00`).toLocaleDateString('tr-TR')} icin saat bazli durum</p>
        <MeetingTimeboard slots={selectedDaySlots} />
      </article>

      <article className="page-card meeting-calendar-card">
        <h3>Onay Bekleyen Talepler</h3>
        <div className="panel-list-grid">
          {requestedItems.length === 0 ? <p>Onay bekleyen randevu talebi yok.</p> : null}
          {requestedItems.map((item) => (
            <article key={item.applicationId} className="page-card panel-list-card">
              <h4>{item.grantTitle ?? `Basvuru #${item.applicationId}`}</h4>
              <p>Firma: {item.firmUsername ?? '-'}</p>
              <p>Talep: {formatDateTime(item.requestedMeetingAt)}</p>
              <p>Basvuru Zamani: {formatDateTime(item.submittedAt)}</p>
              <textarea
                rows={2}
                placeholder="Onay notu"
                value={noteByApp[item.applicationId] ?? item.meetingNote ?? ''}
                onChange={(event) => setNoteByApp((prev) => ({ ...prev, [item.applicationId]: event.target.value }))}
              />
              <div className="meeting-inline-actions">
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => void onConfirmMeeting(item.applicationId, item.requestedMeetingAt)}
                >
                  Talep Edilen Saati Onayla
                </button>
                <button
                  type="button"
                  className="btn"
                  onClick={() => void onRejectMeeting(item.applicationId, item.requestedMeetingAt)}
                >
                  Talebi Reddet
                </button>
              </div>
            </article>
          ))}
        </div>
      </article>

      <article className="page-card meeting-calendar-card">
        <h3>Onayli Randevular (Iptal)</h3>
        <div className="panel-list-grid">
          {confirmedItems.length === 0 ? <p>Onayli randevu yok.</p> : null}
          {confirmedItems.map((item) => (
            <article key={`confirmed-${item.applicationId}`} className="page-card panel-list-card">
              <h4>{item.grantTitle ?? `Basvuru #${item.applicationId}`}</h4>
              <p>Firma: {item.firmUsername ?? '-'}</p>
              <p>Onayli Saat: {formatDateTime(item.confirmedMeetingAt)}</p>
              <p>Talep: {formatDateTime(item.requestedMeetingAt)}</p>
              <textarea
                rows={2}
                placeholder="Iptal notu (firmaya bildirim olarak gider)"
                value={noteByApp[item.applicationId] ?? ''}
                onChange={(event) => setNoteByApp((prev) => ({ ...prev, [item.applicationId]: event.target.value }))}
              />
              <button
                type="button"
                className="btn"
                onClick={() => void onCancelMeeting(item.applicationId, item.confirmedMeetingAt)}
              >
                Randevuyu Iptal Et
              </button>
            </article>
          ))}
        </div>
      </article>
    </section>
  )
}
