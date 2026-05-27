import { useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/features/auth/model/use-auth'

type ChipKey = 'institution' | 'nace' | 'minFunding' | 'maxFunding' | 'budget' | 'employees' | 'turnover'
type SearchChip = { key: ChipKey; label: string; value: string }

function parseMoney(value: string): number | undefined {
  const normalized = value.replace(/[\.\s,]/g, '')
  if (!/^\d+$/.test(normalized)) return undefined
  const parsed = Number(normalized)
  return Number.isFinite(parsed) ? parsed : undefined
}

function extractAutoBudgetToken(text: string): string | undefined {
  const trimmed = text.trim()
  if (!trimmed) return undefined

  const standalone = trimmed.match(/^\d[\d.,\s]*$/)?.[0]
  if (standalone) {
    const parsed = parseMoney(standalone)
    if (parsed != null && parsed >= 10000) return standalone.trim()
  }

  const withBudgetPrefix = trimmed.match(/\bb[uü]t[cç]e\s*[:=-]?\s*([\d.,\s]{4,})/i)?.[1]
  if (withBudgetPrefix) {
    const parsed = parseMoney(withBudgetPrefix)
    if (parsed != null && parsed >= 10000) return withBudgetPrefix.trim()
  }

  const withCurrency = trimmed.match(/\b(\d{5,}|\d{1,3}(?:[.,]\d{3})+)\s*(?:tl|try|eur|usd|€|\$)\b/i)?.[1]
  if (withCurrency) {
    const parsed = parseMoney(withCurrency)
    if (parsed != null && parsed >= 10000) return withCurrency
  }

  const currencyFirst = trimmed.match(/(?:tl|try|eur|usd|€|\$)\s*(\d{5,}|\d{1,3}(?:[.,]\d{3})+)/i)?.[1]
  if (currencyFirst) {
    const parsed = parseMoney(currencyFirst)
    if (parsed != null && parsed >= 10000) return currencyFirst
  }

  return undefined
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function sanitizeInstitutionText(value: string): string {
  return value
    .replace(/\b\d{9,}\b/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function buildRobotSearchParams(keyword: string, advanced: Record<string, string>): URLSearchParams {
  const params = new URLSearchParams()
  let working = keyword.trim()

  let nace = advanced.nace?.trim() || ''
  if (!nace) {
    const naceMatch = working.match(/\bnace\s*[:=-]?\s*(\d{2}(?:\.\d{2}){0,2})\b/i)
      ?? working.match(/\b(\d{2}(?:\.\d{2}){0,2})\b/)
    nace = naceMatch?.[1] ?? ''
  }
  if (nace) {
    params.set('nace', nace)
    working = working
      .replace(/\bnace\s*[:=-]?\s*\d{2}(?:\.\d{2}){0,2}\b/gi, ' ')
      .replace(/\b\d{2}(?:\.\d{2}){0,2}\b/g, ' ')
      .replace(/\s+/g, ' ')
      .trim()
  }

  const budgetToken = extractAutoBudgetToken(working)
  const autoBudget = budgetToken ? parseMoney(budgetToken) : undefined

  if (advanced.minFunding.trim()) params.set('minFunding', advanced.minFunding.trim())
  if (advanced.maxFunding.trim()) params.set('maxFunding', advanced.maxFunding.trim())

  if (!advanced.minFunding.trim() && !advanced.maxFunding.trim() && autoBudget != null) {
    params.set('minFunding', String(autoBudget))
    params.set('maxFunding', String(autoBudget))
    working = working
      .replace(/\b(?:\d{5,}|\d{1,3}(?:[.,]\d{3})+)\b/g, ' ')
      .replace(/\s+/g, ' ')
      .trim()
  }

  working = sanitizeInstitutionText(working)

  const institution = sanitizeInstitutionText(advanced.institution.trim()) || working
  if (institution) params.set('institution', institution)
  if (working) params.set('q', working)

  if (advanced.employees.trim()) params.set('employees', advanced.employees.trim())
  if (advanced.turnover.trim()) params.set('turnover', advanced.turnover.trim())

  return params
}

function getRobotChips(keyword: string, advanced: Record<string, string>): SearchChip[] {
  const chips: SearchChip[] = []
  const naceMatch = keyword.match(/\bnace\s*[:=-]?\s*(\d{2}(?:\.\d{2}){0,2})\b/i)
    ?? keyword.match(/\b(\d{2}(?:\.\d{2}){0,2})\b/)

  const nace = advanced.nace?.trim() || naceMatch?.[1]
  if (nace) chips.push({ key: 'nace', label: 'NACE', value: nace.toUpperCase() })

  const budgetToken = extractAutoBudgetToken(keyword)
  const budget = budgetToken ? parseMoney(budgetToken) : undefined
  const minFunding = advanced.minFunding?.trim() ? Number(advanced.minFunding) : undefined
  const maxFunding = advanced.maxFunding?.trim() ? Number(advanced.maxFunding) : undefined
  if (budget != null) {
    chips.push({ key: 'budget', label: 'Butce', value: budget.toLocaleString('tr-TR') })
  } else if (minFunding != null && maxFunding != null && minFunding === maxFunding) {
    chips.push({ key: 'budget', label: 'Butce', value: minFunding.toLocaleString('tr-TR') })
  } else {
    if (minFunding != null && Number.isFinite(minFunding)) {
      chips.push({ key: 'minFunding', label: 'Min Butce', value: minFunding.toLocaleString('tr-TR') })
    }
    if (maxFunding != null && Number.isFinite(maxFunding)) {
      chips.push({ key: 'maxFunding', label: 'Max Butce', value: maxFunding.toLocaleString('tr-TR') })
    }
  }

  const text = sanitizeInstitutionText(keyword
    .replace(/\bnace\s*[:=-]?\s*\d{2}(?:\.\d{2}){0,2}\b/i, ' ')
    .replace(/\b\d{2}(?:\.\d{2}){0,2}\b/g, ' ')
    .replace(/\b(?:\d{5,}|\d{1,3}(?:[.,]\d{3})+)\b/g, ' ')
    .replace(/\s+/g, ' ')
    .trim())

  if (advanced.institution?.trim()) {
    chips.push({ key: 'institution', label: 'Kurum', value: sanitizeInstitutionText(advanced.institution.trim()).toUpperCase() })
  } else if (text) {
    chips.push({ key: 'institution', label: 'Kurum', value: text.toUpperCase() })
  }

  if (advanced.employees?.trim()) chips.push({ key: 'employees', label: 'Calisan', value: advanced.employees.trim() })
  if (advanced.turnover?.trim()) chips.push({ key: 'turnover', label: 'Ciro', value: advanced.turnover.trim() })

  return chips.filter((chip, index) =>
    chips.findIndex((candidate) => candidate.label === chip.label && candidate.value === chip.value) === index,
  )
}

export function RobotPage() {
  const navigate = useNavigate()
  const { role } = useAuth()
  const [keyword, setKeyword] = useState('')
  const [showAdvanced, setShowAdvanced] = useState(false)
  const [advanced, setAdvanced] = useState({
    institution: '',
    nace: '',
    minFunding: '',
    maxFunding: '',
    employees: '',
    turnover: '',
  })
  const previewChips = useMemo(() => getRobotChips(keyword, advanced), [keyword, advanced])

  function onChipRemove(chip: SearchChip) {
    const nextAdvanced = { ...advanced }
    let nextKeyword = keyword

    if (chip.key === 'nace') {
      nextAdvanced.nace = ''
      nextKeyword = nextKeyword
        .replace(/\bnace\s*[:=-]?\s*\d{2}(?:\.\d{2}){0,2}\b/gi, ' ')
        .replace(/\b\d{2}(?:\.\d{2}){0,2}\b/g, ' ')
        .replace(/\s+/g, ' ')
        .trim()
    }

    if (chip.key === 'budget' || chip.key === 'minFunding' || chip.key === 'maxFunding') {
      nextAdvanced.minFunding = ''
      nextAdvanced.maxFunding = ''
      nextKeyword = nextKeyword
        .replace(/\bb[uü]t[cç]e\s*[:=-]?\s*[\d\.,\s]+/gi, ' ')
        .replace(/\b(?:\d{5,}|\d{1,3}(?:[.,]\d{3})+)\b/g, ' ')
        .replace(/\s+/g, ' ')
        .trim()
    }

    if (chip.key === 'institution') {
      nextAdvanced.institution = ''
      const escaped = escapeRegExp(chip.value)
      nextKeyword = nextKeyword.replace(new RegExp(escaped, 'i'), ' ').replace(/\s+/g, ' ').trim()
    }

    if (chip.key === 'employees') nextAdvanced.employees = ''
    if (chip.key === 'turnover') nextAdvanced.turnover = ''

    setAdvanced(nextAdvanced)
    setKeyword(nextKeyword)
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const params = buildRobotSearchParams(keyword, advanced)
    const targetBase = role === 'FIRMA' ? '/app/robot/results' : '/grants'
    navigate(`${targetBase}?${params.toString()}`)
  }

  return (
    <section className="search-hero page-card">
      <h1>Teşvik Robotu</h1>
      <p>Önce tek arama yapın. Sonuç fazla gelirse gelişmiş arama ile daraltın.</p>

      <form onSubmit={onSubmit} className="search-shell-form">
        <div className="search-shell">
          <input
            className="search-shell-input"
            placeholder="Aradığınız destek ile ilgili anahtar sözcük veya kurum ismi yazın"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button type="button" className="btn search-shell-advanced" onClick={() => setShowAdvanced((prev) => !prev)}>
            {showAdvanced ? 'Gelişmişi Gizle' : 'Gelişmiş Arama'}
          </button>
          <button type="submit" className="btn btn-primary search-shell-submit">Ara</button>
        </div>

        {previewChips.length > 0 ? (
          <div className="search-chip-row">
            {previewChips.map((chip, index) => (
              <button
                key={`${chip.key}-${chip.label}-${chip.value}-${index}`}
                type="button"
                className="search-chip"
                onClick={() => onChipRemove(chip)}
              >
                {chip.label}:{chip.value} x
              </button>
            ))}
          </div>
        ) : null}

        {showAdvanced ? (
          <div className="advanced-grid">
            <input placeholder="Kurum / Program (örn: KOSGEB)" value={advanced.institution} onChange={(event) => setAdvanced((prev) => ({ ...prev, institution: event.target.value }))} />
            <input placeholder="NACE Kodu (örn: 62.01)" value={advanced.nace} onChange={(event) => setAdvanced((prev) => ({ ...prev, nace: event.target.value }))} />
            <input type="number" placeholder="Min Bütçe" value={advanced.minFunding} onChange={(event) => setAdvanced((prev) => ({ ...prev, minFunding: event.target.value }))} />
            <input type="number" placeholder="Max Bütçe" value={advanced.maxFunding} onChange={(event) => setAdvanced((prev) => ({ ...prev, maxFunding: event.target.value }))} />
            <input type="number" placeholder="Çalışan Sayısı" value={advanced.employees} onChange={(event) => setAdvanced((prev) => ({ ...prev, employees: event.target.value }))} />
            <input type="number" placeholder="Yıllık Ciro" value={advanced.turnover} onChange={(event) => setAdvanced((prev) => ({ ...prev, turnover: event.target.value }))} />
          </div>
        ) : null}
      </form>
    </section>
  )
}
