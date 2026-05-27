import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import axios from 'axios'
import {
  listMyMeetingCalendar,
  listOccupiedMeetingTimes,
  listUnavailableMeetingDays,
  requestMeetingDirect,
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

function toDateTimeKey(value: string): string {
  return value.slice(0, 16)
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

function buildHalfHourTimeOptions(): string[] {
  const options: string[] = []
  for (let hour = 9; hour <= 17; hour += 1) {
    options.push(`${String(hour).padStart(2, '0')}:00`)
  }
  return options
}

export function FirmMeetingsPage() {
  const [calendarItems, setCalendarItems] = useState<MeetingCalendarItem[]>([])
  const [unavailableDays, setUnavailableDays] = useState<string[]>([])
  const [occupiedTimes, setOccupiedTimes] = useState<string[]>([])
  const [selectedDayKey, setSelectedDayKey] = useState(() => toDateKey(new Date()))
  const [selectedTime, setSelectedTime] = useState<string | null>(null)
  const [note, setNote] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [calendarMonth, setCalendarMonth] = useState(() => new Date())
  const [timePanelPulse, setTimePanelPulse] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [confirmedSort, setConfirmedSort] = useState<'ASC' | 'DESC'>('ASC')
  const [pastSort, setPastSort] = useState<'ASC' | 'DESC'>('DESC')
  const [pastFilter, setPastFilter] = useState<'ALL' | 'COMPLETED' | 'REJECTED'>('ALL')
  const timePanelRef = useRef<HTMLElement | null>(null)

  const load = useCallback(async () => {
    setIsLoading(true)
    setError('')
    setMessage('')

    const [meetingsRes, unavailableRes, occupiedRes] = await Promise.allSettled([
      listMyMeetingCalendar(),
      listUnavailableMeetingDays(false),
      listOccupiedMeetingTimes(),
    ])

    if (meetingsRes.status === 'fulfilled') {
      setCalendarItems(meetingsRes.value)
    } else {
      setCalendarItems([])
    }

    if (unavailableRes.status === 'fulfilled') {
      setUnavailableDays(unavailableRes.value)
    } else {
      setUnavailableDays([])
    }

    if (occupiedRes.status === 'fulfilled') {
      setOccupiedTimes(occupiedRes.value)
    } else {
      setOccupiedTimes([])
    }

    const allRejected =
      meetingsRes.status === 'rejected' &&
      unavailableRes.status === 'rejected' &&
      occupiedRes.status === 'rejected'

    if (allRejected) {
      const reasons = [meetingsRes, unavailableRes, occupiedRes]
        .filter((r): r is PromiseRejectedResult => r.status === 'rejected')
        .map((r) => r.reason)

      const authError = reasons.find((reason) => axios.isAxiosError(reason) && reason.response?.status === 401)
      if (authError) {
        setError('Oturum suresi dolmus olabilir. Lutfen yeniden giris yapin.')
      } else {
        const networkError = reasons.find((reason) => axios.isAxiosError(reason) && !reason.response)
        if (networkError) {
          setError('Sunucuya ulasilamadi. Backend servisini kontrol edin.')
        } else {
          setError('Randevu verileri su anda yuklenemiyor. Lutfen sayfayi yenileyin.')
        }
      }
    }

    setIsLoading(false)
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const unavailableDaySet = useMemo(() => new Set(unavailableDays), [unavailableDays])
  const monthDays = useMemo(() => buildMonthDays(calendarMonth), [calendarMonth])
  const timeOptions = useMemo(() => buildHalfHourTimeOptions(), [])

  const occupiedTimeSet = useMemo(
    () => new Set(occupiedTimes.map((v) => toDateTimeKey(v))),
    [occupiedTimes],
  )

  const myMeetingsByDateTime = useMemo(() => {
    const map = new Map<string, MeetingCalendarItem>()
    for (const item of calendarItems) {
      if (item.meetingStatus === 'REJECTED') continue
      const when = item.effectiveMeetingAt
      if (!when) continue
      map.set(toDateTimeKey(when), item)
    }
    return map
  }, [calendarItems])

  const pendingRequestCount = useMemo(
    () => calendarItems.filter((item) => item.meetingStatus === 'REQUESTED').length,
    [calendarItems],
  )

  const selectedDateDisplay = useMemo(
    () => new Date(`${selectedDayKey}T00:00:00`).toLocaleDateString('tr-TR'),
    [selectedDayKey],
  )

  const pendingMeetings = useMemo(
    () => calendarItems.filter((item) => item.meetingStatus === 'REQUESTED' && !!item.requestedMeetingAt),
    [calendarItems],
  )

  const confirmedMeetings = useMemo(() => {
    const upcomingConfirmed = calendarItems
      .filter(
        (item) => item.meetingStatus === 'CONFIRMED' && !!item.confirmedMeetingAt && new Date(item.confirmedMeetingAt).getTime() >= Date.now(),
      )
      .sort((a, b) => {
        const aTs = new Date(a.confirmedMeetingAt ?? 0).getTime()
        const bTs = new Date(b.confirmedMeetingAt ?? 0).getTime()
        return confirmedSort === 'ASC' ? aTs - bTs : bTs - aTs
      })
    return upcomingConfirmed
  }, [calendarItems, confirmedSort])

  const pastMeetings = useMemo(() => {
    const history = calendarItems
      .filter((item) => {
        if (item.meetingStatus === 'REJECTED') return true
        if (!item.confirmedMeetingAt) return false
        return new Date(item.confirmedMeetingAt).getTime() < Date.now()
      })
      .filter((item) => {
        if (pastFilter === 'ALL') return true
        if (pastFilter === 'REJECTED') return item.meetingStatus === 'REJECTED'
        return item.meetingStatus !== 'REJECTED'
      })
      .sort((a, b) => {
        const aRef = new Date(a.confirmedMeetingAt ?? a.requestedMeetingAt ?? 0).getTime()
        const bRef = new Date(b.confirmedMeetingAt ?? b.requestedMeetingAt ?? 0).getTime()
        return pastSort === 'ASC' ? aRef - bRef : bRef - aRef
      })

    return history
  }, [calendarItems, pastFilter, pastSort])

  const selectedDaySlots = useMemo<MeetingTimeboardSlot[]>(() => {
    return timeOptions.map((time) => {
      const dateTimeKey = `${selectedDayKey}T${time}`
      const myMeeting = myMeetingsByDateTime.get(dateTimeKey)
      const status = myMeeting
        ? myMeeting.meetingStatus === 'CONFIRMED'
          ? { state: 'CONFIRMED' as const, label: 'Onaylandi' }
          : { state: 'REQUESTED' as const, label: 'Onay Bekliyor' }
        : occupiedTimeSet.has(dateTimeKey)
          ? { state: 'OCCUPIED' as const, label: 'Dolu' }
          : { state: 'AVAILABLE' as const, label: 'Bos' }
      const isDisabled = status.state !== 'AVAILABLE'
      const info = myMeetingsByDateTime.get(`${selectedDayKey}T${time}`)
      return {
        key: `firm-${selectedDayKey}-${time}`,
        time,
        label: status.label,
        state: status.state,
        selected: selectedTime === time,
        disabled: isDisabled,
        onClick: () => {
          if (isDisabled) return
          setSelectedTime(time)
          setMessage(`Saat secildi: ${time}. Randevu talebi olusturabilirsiniz.`)
        },
        tooltipTitle: status.state === 'AVAILABLE' ? 'Uygun Saat' : 'Saat Durumu',
        tooltipLines: [
          `Durum: ${status.label}`,
          `Talep: ${formatDateTime(info?.requestedMeetingAt)}`,
          `Not: ${info?.meetingNote?.trim() ? info.meetingNote : '-'}`,
        ],
      }
    })
  }, [timeOptions, selectedDayKey, selectedTime, myMeetingsByDateTime, occupiedTimeSet])
  async function submitMeetingRequest() {
    if (pendingRequestCount >= 3) {
      setError('En fazla 3 onay bekleyen randevu talebi acabilirsiniz. Lutfen once bekleyen taleplerin sonucunu bekleyin.')
      return
    }

    if (!selectedTime) {
      setError('Lutfen saat secin.')
      return
    }

    const requestedMeetingAt = `${selectedDayKey}T${selectedTime}:00`
    if (occupiedTimeSet.has(requestedMeetingAt.slice(0, 16))) {
      setError('Bu saat dolu. Baska bir saat secin.')
      return
    }

    setIsSubmitting(true)
    setError('')
    setMessage('')
    try {
      await requestMeetingDirect(requestedMeetingAt, note)
      setMessage(`Talebiniz alindi: ${formatDateTime(requestedMeetingAt)} (onay bekliyor).`)
      setSelectedTime(null)
      setNote('')
      await load()
    } catch (err) {
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 404 && err.config?.url?.includes('/api/applications/meeting')) {
          setError('Randevu endpointi bulunamadi. Backend eski surumde olabilir; servisi durdurup yeniden baslatin.')
          return
        }
        if (err.response?.status === 401) {
          setError('Oturum suresi dolmus olabilir. Yeniden giris yapin.')
          return
        }
        const backendMessage = (err.response?.data as { message?: string } | undefined)?.message
        if (backendMessage?.trim()) {
          setError(backendMessage)
          return
        }
        if (!err.response) {
          setError('Sunucuya ulasilamadi. Backend servisini kontrol edin.')
          return
        }
      }
      setError('Talep olusturulamadi. Saat dolu olabilir veya daha once talep acmis olabilirsiniz.')
    } finally {
      setIsSubmitting(false)
    }
  }

  function onSelectDay(dayKey: string) {
    setSelectedDayKey(dayKey)
    setSelectedTime(null)
    setMessage(`Gun secildi (${new Date(`${dayKey}T00:00:00`).toLocaleDateString('tr-TR')}). Simdi asagidan saat secin.`)
    setTimePanelPulse(true)
    window.setTimeout(() => setTimePanelPulse(false), 850)
    window.setTimeout(() => {
      timePanelRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }, 80)
  }

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Randevularim</h1>
        <p>Takvimden gun secin, saat secin ve talep olusturun. Admin onaylarsa saat tum firmalara kapanir.</p>
      </div>

      {message ? <p className="panel-success">{message}</p> : null}
      {error ? <p className="panel-error">{error}</p> : null}
      {isLoading ? <p>Yukleniyor...</p> : null}

      <article className="page-card meeting-calendar-card">
        <div className="meeting-calendar-head">
          <h3>Aylik Takvim (Gun Secimi)</h3>
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
            return (
              <button
                key={key}
                type="button"
                className={`meeting-day-cell ${isCurrentMonth ? '' : 'is-out'} ${isUnavailable ? 'is-unavailable' : 'is-available'} ${selectedDayKey === key ? 'is-selected-day' : ''}`}
                onClick={() => onSelectDay(key)}
              >
                <span>{day.getDate()}</span>
              </button>
            )
          })}
        </div>
      </article>

      <article ref={timePanelRef} className={`page-card meeting-calendar-card ${timePanelPulse ? 'is-pulse' : ''}`}>
        <h3>Gunluk Saat Secimi</h3>
        <p className="meeting-help-text">Secilen gun: {selectedDateDisplay}</p>
        <p className="meeting-help-text">
          Secilen saat: {selectedTime ?? 'Secilmedi'}
        </p>

        <div className="meeting-form-grid">
          <label className="panel-form-span-2">
            Not (opsiyonel)
            <textarea
              rows={2}
              value={note}
              onChange={(event) => setNote(event.target.value)}
              placeholder="Kisa not"
            />
          </label>
        </div>

        <MeetingTimeboard slots={selectedDaySlots} />

        <div className="meeting-inline-actions">
          <button
            type="button"
            className="btn btn-primary"
            disabled={!selectedTime || isSubmitting || pendingRequestCount >= 3}
            onClick={() => void submitMeetingRequest()}
          >
            {isSubmitting ? 'Gonderiliyor...' : 'Randevu Talebi Olustur'}
          </button>
        </div>
      </article>

      <article className="page-card meeting-calendar-card meetings-dashboard-card">
        <h3>Randevu Durumu</h3>
        <div className="meetings-overview-grid">
          <div className="meeting-kpi-card is-pending">
            <span>Onay Bekleyenler</span>
            <strong>{pendingMeetings.length}</strong>
          </div>
          <div className="meeting-kpi-card is-confirmed">
            <span>Onaylananlar</span>
            <strong>{confirmedMeetings.length}</strong>
          </div>
          <div className="meeting-kpi-card is-past">
            <span>Gecmis Randevularim</span>
            <strong>{pastMeetings.length}</strong>
          </div>
        </div>
      </article>

      <section className="meetings-history-grid">
        <article className="page-card meeting-calendar-card meeting-history-column">
          <h3>Onay Bekleyenler</h3>
          {pendingMeetings.length === 0 ? <p>Onay bekleyen randevu talebiniz yok.</p> : null}
          <div className="meeting-history-list">
            {pendingMeetings.map((item) => (
              <article key={`pending-${item.applicationId}`} className="meeting-history-item">
                <h4>{item.grantTitle ?? `Basvuru #${item.applicationId}`}</h4>
                <p>Talep: {formatDateTime(item.requestedMeetingAt)}</p>
                <p>Not: {item.meetingNote ?? '-'}</p>
              </article>
            ))}
          </div>
        </article>

        <article className="page-card meeting-calendar-card meeting-history-column">
          <h3>Onaylananlar</h3>
          <div className="meeting-history-toolbar">
            <label>
              Siralama
              <select value={confirmedSort} onChange={(event) => setConfirmedSort(event.target.value as 'ASC' | 'DESC')}>
                <option value="ASC">Tarih (Eskiden Yeniye)</option>
                <option value="DESC">Tarih (Yeniden Eskiye)</option>
              </select>
            </label>
          </div>
          {confirmedMeetings.length === 0 ? <p>Yaklasan onayli randevunuz yok.</p> : null}
          <div className="meeting-history-list timeline-list">
            {confirmedMeetings.map((item) => (
              <article key={`confirmed-${item.applicationId}`} className="meeting-history-item timeline-item">
                <h4>{item.grantTitle ?? `Basvuru #${item.applicationId}`}</h4>
                <div className="meeting-badge-row">
                  <span className="meeting-status-badge is-confirmed">Onaylandi</span>
                </div>
                <p>Onayli Saat: {formatDateTime(item.confirmedMeetingAt)}</p>
                <p>Talep: {formatDateTime(item.requestedMeetingAt)}</p>
                <p>Not: {item.meetingNote ?? '-'}</p>
              </article>
            ))}
          </div>
        </article>

        <article className="page-card meeting-calendar-card meeting-history-column">
          <h3>Gecmis Randevularim</h3>
          <div className="meeting-history-toolbar">
            <label>
              Filtre
              <select value={pastFilter} onChange={(event) => setPastFilter(event.target.value as 'ALL' | 'COMPLETED' | 'REJECTED')}>
                <option value="ALL">Hepsi</option>
                <option value="COMPLETED">Tamamlanan</option>
                <option value="REJECTED">Reddedilen</option>
              </select>
            </label>
            <label>
              Siralama
              <select value={pastSort} onChange={(event) => setPastSort(event.target.value as 'ASC' | 'DESC')}>
                <option value="DESC">Tarih (Yeniden Eskiye)</option>
                <option value="ASC">Tarih (Eskiden Yeniye)</option>
              </select>
            </label>
          </div>
          {pastMeetings.length === 0 ? <p>Gecmis randevu kaydiniz yok.</p> : null}
          <div className="meeting-history-list timeline-list">
            {pastMeetings.map((item) => (
              <article key={`past-${item.applicationId}`} className="meeting-history-item timeline-item">
                <h4>{item.grantTitle ?? `Basvuru #${item.applicationId}`}</h4>
                <div className="meeting-badge-row">
                  <span className={`meeting-status-badge ${item.meetingStatus === 'REJECTED' ? 'is-rejected' : 'is-completed'}`}>
                    {item.meetingStatus === 'REJECTED' ? 'Reddedildi' : 'Tamamlandi'}
                  </span>
                </div>
                <p>Talep: {formatDateTime(item.requestedMeetingAt)}</p>
                <p>Onayli: {formatDateTime(item.confirmedMeetingAt)}</p>
                <p>Not: {item.meetingNote ?? '-'}</p>
              </article>
            ))}
          </div>
        </article>
      </section>
    </section>
  )
}
