type MeetingTimeState = 'AVAILABLE' | 'OCCUPIED' | 'REQUESTED' | 'CONFIRMED'

export type MeetingTimeboardSlot = {
  key?: string
  time: string
  label: string
  state: MeetingTimeState
  selected?: boolean
  disabled?: boolean
  onClick?: () => void
  tooltipTitle?: string
  tooltipLines?: string[]
}

function toStateClass(state: MeetingTimeState): string {
  switch (state) {
    case 'AVAILABLE':
      return 'is-available'
    case 'OCCUPIED':
      return 'is-occupied'
    case 'REQUESTED':
      return 'is-requested'
    case 'CONFIRMED':
      return 'is-confirmed'
    default:
      return 'is-available'
  }
}

export function MeetingTimeboard({ slots }: { slots: MeetingTimeboardSlot[] }) {
  return (
    <div className="meeting-timeboard-grid">
      {slots.map((slot) => {
        const stateClass = toStateClass(slot.state)
        const disabled = slot.disabled ?? slot.state !== 'AVAILABLE'

        return (
          <span key={slot.key ?? slot.time} className="meeting-timeboard-wrap">
            <button
              type="button"
              disabled={disabled}
              className={`meeting-timeboard-btn ${stateClass} ${slot.selected ? 'is-selected' : ''}`}
              onClick={slot.onClick}
            >
              {slot.time} - {slot.label}
            </button>
            {slot.tooltipTitle || (slot.tooltipLines && slot.tooltipLines.length > 0) ? (
              <span className="meeting-timeboard-tooltip" role="tooltip">
                {slot.tooltipTitle ? <strong>{slot.tooltipTitle}</strong> : null}
                {slot.tooltipLines?.map((line) => (
                  <span key={`${slot.key ?? slot.time}-${line}`}>{line}</span>
                ))}
              </span>
            ) : null}
          </span>
        )
      })}
    </div>
  )
}
