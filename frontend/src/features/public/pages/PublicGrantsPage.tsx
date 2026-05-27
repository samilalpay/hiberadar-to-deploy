import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router-dom'
import { checkEligibility, listGrants, type GrantItem } from '@/features/public/api/public.api'
import { useInstitutionsQuery } from '@/features/admin/api/institutions.api'
import type { Institution } from '@/shared/types/institution'
import { resolveMediaUrl } from '@/shared/lib/media'

type EligibilityState = { eligible: boolean | null; reasons: string[] }
type ChipKey = 'institution' | 'nace' | 'minFunding' | 'maxFunding' | 'budget' | 'employees' | 'turnover'
type SearchChip = { key: ChipKey; label: string; value: string }
type Urgency = {
  label: 'Acil' | 'Yaklasiyor' | 'Rahat' | 'Kapali' | 'Suresi Dolmus'
  className: string
  remainingText: string
}

type QuickFilter = 'ALL' | 'Acil' | 'Yaklasiyor' | 'Rahat' | 'Kapali'
type StatusFilter = 'ALL' | 'PUBLISHED' | 'CLOSED'

type SearchInput = {
  q?: string
  nace?: string
  institution?: string
  minFunding?: number
  maxFunding?: number
  chips: SearchChip[]
}

type InstitutionBrand = {
  shortCode: string
  logoSrc?: string | null
}

function parseMoney(value: string): number | undefined {
  const normalized = value.replace(/[.,\s]/g, '')
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

function normalizeForSearch(value: string): string {
  return value
    .toLocaleLowerCase('tr-TR')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/ı/g, 'i')
    .trim()
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

function getUrgency(deadlineAt?: string, status?: string): Urgency {
  if (status === 'CLOSED') {
    return { label: 'Kapali', className: 'urgency-closed', remainingText: 'Cagri kapali' }
  }

  if (!deadlineAt) {
    return { label: 'Rahat', className: 'urgency-relaxed', remainingText: 'Son tarih belirtilmemis' }
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineAt)
  deadline.setHours(0, 0, 0, 0)
  const diffDays = Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return { label: 'Kapali', className: 'urgency-closed', remainingText: `${Math.abs(diffDays)} gun once sona erdi` }
  }
  if (diffDays <= 7) {
    return { label: 'Acil', className: 'urgency-urgent', remainingText: `${diffDays} gun kaldi` }
  }
  if (diffDays <= 21) {
    return { label: 'Yaklasiyor', className: 'urgency-soon', remainingText: `${diffDays} gun kaldi` }
  }
  return { label: 'Rahat', className: 'urgency-relaxed', remainingText: `${diffDays} gun kaldi` }
}

function formatDate(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString('tr-TR')
}


function getRemainingDays(deadlineAt?: string): number | null {
  if (!deadlineAt) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineAt)
  deadline.setHours(0, 0, 0, 0)
  if (Number.isNaN(deadline.getTime())) return null
  return Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
}

function getSortDaysValue(item: GrantItem): number {
  const isClosed = item.status === 'CLOSED' || item.clickable === false
  if (isClosed) return Number.MAX_SAFE_INTEGER

  const remaining = getRemainingDays(item.deadlineAt)
  if (remaining == null) return Number.MAX_SAFE_INTEGER - 2
  if (remaining < 0) return Number.MAX_SAFE_INTEGER - 1
  return remaining
}

function matchesInstitution(grant: GrantItem, institutionText?: string): boolean {
  if (!institutionText?.trim()) return true

  const haystack = [grant.providerName, grant.programName, grant.title]
    .filter(Boolean)
    .join(' ')
  const normalizedHaystack = normalizeForSearch(haystack)
  const tokens = normalizeForSearch(institutionText)
    .split(/\s+/)
    .map((token) => token.trim())
    .filter((token) => token.length > 0)

  if (tokens.length === 0) return true
  return tokens.every((token) => normalizedHaystack.includes(token))
}

function matchesBudget(grant: GrantItem, minFunding?: number, maxFunding?: number): boolean {
  const grantMin = grant.fundingMin
  const grantMax = grant.fundingMax

  if (minFunding == null && maxFunding == null) {
    return true
  }

  if (minFunding != null && maxFunding == null) {
    // User expects grants that can provide at least this much support.
    return grantMax != null && grantMax >= minFunding
  }

  if (minFunding == null && maxFunding != null) {
    // Only max is set: keep grants whose known upper support limit stays under that ceiling.
    return grantMax != null && grantMax <= maxFunding
  }

  if (grantMin == null && grantMax == null) {
    // Budget range is unknown: do not match when user explicitly filters by budget.
    return false
  }

  // Range search: grant range must overlap with requested range.
  const queryMin = minFunding ?? Number.NEGATIVE_INFINITY
  const queryMax = maxFunding ?? Number.POSITIVE_INFINITY
  const effectiveGrantMin = grantMin ?? 0
  const effectiveGrantMax = grantMax ?? Number.MAX_SAFE_INTEGER
  return effectiveGrantMax >= queryMin && effectiveGrantMin <= queryMax
}

function getRecommendationReasons(item: GrantItem, parsed: SearchInput, urgency: Urgency): string[] {
  const reasons: string[] = []

  if (parsed.nace?.trim()) {
    const expected = parsed.nace.trim().toUpperCase()
    const actual = (item.naceCode ?? '').toUpperCase()
    if (actual === expected || actual.startsWith(`${expected}.`)) {
      reasons.push(`NACE eslesmesi (${parsed.nace})`)
    }
  }

  if (parsed.minFunding != null || parsed.maxFunding != null) {
    const queryMin = parsed.minFunding ?? Number.NEGATIVE_INFINITY
    const queryMax = parsed.maxFunding ?? Number.POSITIVE_INFINITY
    const grantMin = item.fundingMin ?? Number.NEGATIVE_INFINITY
    const grantMax = item.fundingMax ?? Number.POSITIVE_INFINITY
    const overlaps = grantMax >= queryMin && grantMin <= queryMax
    if (overlaps) {
      reasons.push('Butce araligina uyumlu')
    }
  }

  if (urgency.label === 'Acil' || urgency.label === 'Yaklasiyor') {
    reasons.push(`Aciliyet: ${urgency.remainingText}`)
  }

  return reasons
}

function getCardSummary(item: GrantItem): string {
  if (item.summaryShort?.trim()) {
    return item.summaryShort.trim()
  }

  const provider = item.providerName?.trim() || 'Kurum'
  const program = item.programName?.trim() || item.title?.trim() || 'destek programi'
  return `${provider} tarafindan sunulan ${program} cagrisi.`
}

function getInstitutionBrand(item: GrantItem, institutions: Institution[]): InstitutionBrand {
  const provider = item.providerName ?? ''
  const fallback = [item.programName, item.title].filter(Boolean).join(' ')

  const exact = institutions.find((i) => i.name === provider)
  if (exact) return { shortCode: exact.shortCode, logoSrc: resolveMediaUrl(exact.logoUrl) }

  const normProvider = normalizeForSearch(provider)
  const normFallback = normalizeForSearch(fallback)

  const fuzzy = institutions.find((i) => {
    const normName = normalizeForSearch(i.name)
    if (!normName) return false
    return normProvider.includes(normName) || normFallback.includes(normName)
  })

  if (fuzzy) return { shortCode: fuzzy.shortCode, logoSrc: resolveMediaUrl(fuzzy.logoUrl) }

  const initials = (item.providerName ?? item.programName ?? item.title ?? 'Kurum')
    .split(/\s+/)
    .map((part) => part.trim())
    .filter((part) => part.length > 0)
    .slice(0, 3)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')

  return {
    shortCode: initials || 'KR',
    logoSrc: null,
  }
}

function deriveSearchInput(query: string, advanced: Record<string, string>): SearchInput {
  let working = query.trim()
  let nace = advanced.nace?.trim() || undefined
  let institution = sanitizeInstitutionText(advanced.institution?.trim() || '') || undefined
  let minFunding = advanced.minFunding?.trim() ? Number(advanced.minFunding) : undefined
  let maxFunding = advanced.maxFunding?.trim() ? Number(advanced.maxFunding) : undefined

  if (!nace) {
    const nacePrefixMatch = working.match(/\bnace\s*[:=-]?\s*(\d{2}(?:\.\d{2}){0,2})\b/i)
    if (nacePrefixMatch?.[1]) {
      nace = nacePrefixMatch[1]
      working = working.replace(nacePrefixMatch[0], ' ').replace(/\s+/g, ' ').trim()
    }
  }

  if (!nace) {
    const naceTokenRegex = /\b\d{2}(?:\.\d{2}){0,2}\b/g
    const detectedNace = working.match(naceTokenRegex)?.[0]
    if (detectedNace) {
      nace = detectedNace
      working = working.replace(detectedNace, ' ').replace(/\s+/g, ' ').trim()
    }
  }

  if (minFunding == null && maxFunding == null) {
    const numericToken = extractAutoBudgetToken(working)
    const numericBudget = numericToken ? parseMoney(numericToken) : undefined
    if (numericBudget != null && numericToken) {
      minFunding = numericBudget
      maxFunding = numericBudget
      working = working.replace(numericToken, ' ').replace(/\s+/g, ' ').trim()
    }
  }

  if (minFunding == null && maxFunding == null) {
    const budgetPrefixMatch = working.match(/\bb[uü]t[cç]e\s*[:=-]?\s*([\d.,\s]+)/i)
    const prefixedBudget = budgetPrefixMatch?.[1] ? parseMoney(budgetPrefixMatch[1]) : undefined
    if (prefixedBudget != null) {
      minFunding = prefixedBudget
      maxFunding = prefixedBudget
      working = working.replace(budgetPrefixMatch![0], ' ').replace(/\s+/g, ' ').trim()
    }
  }

  if (minFunding == null && maxFunding == null && /^\d[\d.,\s]*$/.test(working)) {
    const standaloneBudget = parseMoney(working)
    if (standaloneBudget != null && standaloneBudget >= 10000) {
      minFunding = standaloneBudget
      maxFunding = standaloneBudget
      working = ''
    }
  }

  working = sanitizeInstitutionText(working)

  if (!institution && working) {
    institution = working
  }

  const chips: SearchChip[] = []
  if (institution) chips.push({ key: 'institution', label: 'Kurum', value: institution.toUpperCase() })
  if (nace) chips.push({ key: 'nace', label: 'NACE', value: nace.toUpperCase() })

  if (minFunding != null && maxFunding != null && minFunding === maxFunding) {
    chips.push({ key: 'budget', label: 'Butce', value: minFunding.toLocaleString('tr-TR') })
  } else {
    if (minFunding != null) chips.push({ key: 'minFunding', label: 'Min Butce', value: minFunding.toLocaleString('tr-TR') })
    if (maxFunding != null) chips.push({ key: 'maxFunding', label: 'Max Butce', value: maxFunding.toLocaleString('tr-TR') })
  }

  if (advanced.employees?.trim()) chips.push({ key: 'employees', label: 'Calisan', value: advanced.employees.trim() })
  if (advanced.turnover?.trim()) chips.push({ key: 'turnover', label: 'Ciro', value: advanced.turnover.trim() })

  return {
    q: working || undefined,
    nace,
    institution,
    minFunding,
    maxFunding,
    chips,
  }
}

function removeCriterionFromQuery(query: string, chip: SearchChip, parsed: SearchInput): string {
  let next = query

  if (chip.key === 'nace') {
    next = next
      .replace(/\bnace\s*[:=-]?\s*\d{2}(?:\.\d{2}){0,2}\b/gi, ' ')
      .replace(/\b\d{2}(?:\.\d{2}){0,2}\b/g, ' ')
  }

  if (chip.key === 'budget' || chip.key === 'minFunding' || chip.key === 'maxFunding') {
    next = next
      .replace(/\bb[uü]t[cç]e\s*[:=-]?\s*[\d.,\s]+/gi, ' ')
      .replace(/\b(?:\d{5,}|\d{1,3}(?:[.,]\d{3})+)\b/g, ' ')
  }

  if (chip.key === 'institution' && parsed.institution) {
    const escaped = escapeRegExp(parsed.institution)
    next = next.replace(new RegExp(escaped, 'i'), ' ')
  }

  return next.replace(/\s+/g, ' ').trim()
}

async function fetchAllGrantsByStatus(
  status: 'PUBLISHED' | 'CLOSED',
  searchInput: SearchInput,
): Promise<{ items: GrantItem[]; total: number }> {
  const PAGE_SIZE = 100
  let pageIndex = 0
  let accumulated: GrantItem[] = []
  let total = 0

  while (true) {
    const page = await listGrants({
      q: searchInput.q,
      nace: searchInput.nace,
      minFunding: searchInput.minFunding,
      maxFunding: searchInput.maxFunding,
      status,
      page: pageIndex,
      size: PAGE_SIZE,
    })
    if (pageIndex === 0) {
      total = page.totalElements
    }
    accumulated = [...accumulated, ...page.content]
    if (accumulated.length >= total || page.content.length < PAGE_SIZE) {
      break
    }
    pageIndex += 1
  }

  return { items: accumulated, total }
}

async function loadPublicGrants(
  query: string,
  advanced: Record<string, string>,
  mode: 'ALL' | 'PUBLISHED_ONLY',
): Promise<{ items: GrantItem[] }> {
  const searchInput = deriveSearchInput(query, advanced)

  const [published, closed] = await Promise.all([
    fetchAllGrantsByStatus('PUBLISHED', searchInput),
    mode === 'PUBLISHED_ONLY'
      ? Promise.resolve({ items: [], total: 0 })
      : fetchAllGrantsByStatus('CLOSED', searchInput),
  ])

  let combined = [...published.items, ...closed.items]
  // If strict text search returns nothing, retry with broader query and apply client-side token matching.
  if (combined.length === 0 && searchInput.institution && mode !== 'PUBLISHED_ONLY') {
    const [fallbackPublished, fallbackClosed] = await Promise.all([
      fetchAllGrantsByStatus('PUBLISHED', { ...searchInput, q: undefined }),
      fetchAllGrantsByStatus('CLOSED', { ...searchInput, q: undefined }),
    ])
    combined = [...fallbackPublished.items, ...fallbackClosed.items]
  }

  return { items: combined }
}

function hasProfileInputs(input: Record<string, string>): boolean {
  return Boolean(
    input.employees ||
    input.turnover
  )
}

function getActiveRefinementCount(advanced: Record<string, string>): number {
  const keys = ['institution', 'nace', 'minFunding', 'maxFunding', 'employees', 'turnover']
  return keys.filter((key) => Boolean(advanced[key]?.trim())).length
}

function getEvaluatedRefinementCount(advanced: Record<string, string>, eligibility?: EligibilityState): number {
  let count = 0
  if (advanced.institution?.trim()) count += 1
  if (advanced.nace?.trim()) count += 1
  if (advanced.minFunding?.trim()) count += 1
  if (advanced.maxFunding?.trim()) count += 1
  if (advanced.employees?.trim() && eligibility?.eligible !== null) count += 1
  if (advanced.turnover?.trim() && eligibility?.eligible !== null) count += 1
  return count
}

function getGrantRefinementMatchCount(
  grant: GrantItem,
  advanced: Record<string, string>,
  eligibility?: EligibilityState,
): number {
  let count = 0

  if (advanced.institution?.trim()) {
    count += matchesInstitution(grant, advanced.institution) ? 1 : 0
  }

  if (advanced.nace?.trim()) {
    const expected = advanced.nace.trim().toUpperCase()
    const actual = (grant.naceCode ?? '').toUpperCase()
    count += actual === expected || actual.startsWith(`${expected}.`) ? 1 : 0
  }

  if (advanced.minFunding?.trim()) {
    const min = Number(advanced.minFunding)
    count += matchesBudget(grant, min, undefined) ? 1 : 0
  }

  if (advanced.maxFunding?.trim()) {
    const max = Number(advanced.maxFunding)
    count += matchesBudget(grant, undefined, max) ? 1 : 0
  }

  if (advanced.employees?.trim()) {
    if (eligibility?.eligible === null) {
      // Eligibility rule missing: this criterion is skipped for this grant.
    } else {
    const reasons = eligibility?.reasons ?? []
    const employeeMismatch = reasons.some((reason) => reason.toLowerCase().includes('employee count'))
    count += employeeMismatch ? 0 : 1
    }
  }

  if (advanced.turnover?.trim()) {
    if (eligibility?.eligible === null) {
      // Eligibility rule missing: this criterion is skipped for this grant.
    } else {
    const reasons = eligibility?.reasons ?? []
    const turnoverMismatch = reasons.some((reason) => reason.toLowerCase().includes('turnover'))
    count += turnoverMismatch ? 0 : 1
    }
  }

  return count
}

export function PublicGrantsPage({ mode = 'ALL' }: { mode?: 'ALL' | 'PUBLISHED_ONLY' }) {
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const [query, setQuery] = useState('')
  const [advanced, setAdvanced] = useState<Record<string, string>>({})
  const [showAdvanced, setShowAdvanced] = useState(false)
  const [items, setItems] = useState<GrantItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [eligibilityMap, setEligibilityMap] = useState<Record<number, EligibilityState>>({})
  const [quickFilter, setQuickFilter] = useState<QuickFilter>('ALL')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>(
    mode === 'PUBLISHED_ONLY' ? 'PUBLISHED' : 'ALL',
  )
  const { data: institutions = [] } = useInstitutionsQuery()
  const parsedPreview = useMemo(() => deriveSearchInput(query, advanced), [query, advanced])
  const detailBasePath = location.pathname.startsWith('/app/') ? '/app/grants' : '/grants'
  const showPublishedOnly = mode === 'PUBLISHED_ONLY'
  const isPublicRoute = !location.pathname.startsWith('/app/') && !location.pathname.startsWith('/admin/')

  const activeRefinementCount = useMemo(() => getActiveRefinementCount(advanced), [advanced])
  const sortedItems = useMemo(() => {
    return [...items].sort((a, b) => {
      // Primary sort: fewer remaining days first, then increasing.
      const byDays = getSortDaysValue(a) - getSortDaysValue(b)
      if (byDays !== 0) {
        return byDays
      }

      const aCount = getGrantRefinementMatchCount(a, advanced, eligibilityMap[a.id])
      const bCount = getGrantRefinementMatchCount(b, advanced, eligibilityMap[b.id])
      return bCount - aCount
    })
  }, [items, advanced, eligibilityMap])

  const urgencyStats = useMemo(() => {
    const counts = {
      acil: 0,
      yaklasiyor: 0,
      rahat: 0,
      kapali: 0,
    }
    items.forEach((item) => {
      const urgency = getUrgency(item.deadlineAt, item.status)
      if (urgency.label === 'Acil') counts.acil++
      else if (urgency.label === 'Yaklasiyor') counts.yaklasiyor++
      else if (urgency.label === 'Rahat') counts.rahat++
      else if (urgency.label === 'Kapali') counts.kapali++
    })
    return counts
  }, [items])

  const visibleItems = useMemo(() => {
    return sortedItems.filter((item) => {
      const effectiveStatus = item.status ?? 'PUBLISHED'
      if (statusFilter !== 'ALL' && effectiveStatus !== statusFilter) {
        return false
      }
      const urgency = getUrgency(item.deadlineAt, item.status)

      if (quickFilter === 'ALL') {
        return true
      }
      return urgency.label === quickFilter
    })
  }, [sortedItems, quickFilter, statusFilter])


  const load = useCallback(async (queryText: string, advancedInput: Record<string, string>) => {
    setIsLoading(true)
    setError('')
    try {
      const data = await loadPublicGrants(queryText, advancedInput, mode)
      const parsed = deriveSearchInput(queryText, advancedInput)
      const narrowedByInstitution = parsed.institution
        ? data.items.filter((grant) => matchesInstitution(grant, parsed.institution))
        : data.items

      const narrowedByBudget = narrowedByInstitution.filter((grant) =>
        matchesBudget(grant, parsed.minFunding, parsed.maxFunding),
      )

      setItems(narrowedByBudget)

      if (!hasProfileInputs(advancedInput)) {
        setEligibilityMap({})
        return
      }

      const checks = await Promise.all(
        narrowedByBudget.map(async (grant) => {
          try {
            const result = await checkEligibility({
              grantId: grant.id,
              employees: advancedInput.employees ? Number(advancedInput.employees) : undefined,
              turnover: advancedInput.turnover ? Number(advancedInput.turnover) : undefined,
            })
            return [grant.id, result] as const
          } catch {
            return [grant.id, { eligible: null, reasons: ['Uygunluk kuralı tanımlı değil'] }] as const
          }
        }),
      )
      const checkMap = Object.fromEntries(checks)
      setEligibilityMap(checkMap)
      const filtered = narrowedByBudget.filter((grant) => checkMap[grant.id]?.eligible === true)
      setItems(filtered)
    } catch {
      setError('Hibeler yüklenemedi.')
    } finally {
      setIsLoading(false)
    }
  }, [mode])


  useEffect(() => {
    const nextQuery = searchParams.get('q') ?? ''
    const nextAdvanced: Record<string, string> = {
      institution: searchParams.get('institution') ?? '',
      nace: searchParams.get('nace') ?? '',
      minFunding: searchParams.get('minFunding') ?? '',
      maxFunding: searchParams.get('maxFunding') ?? '',
      employees: searchParams.get('employees') ?? '',
      turnover: searchParams.get('turnover') ?? '',
    }
    setQuery(nextQuery)
    setAdvanced(nextAdvanced)
    const hasAdvancedParam = Object.values(nextAdvanced).some((value) => Boolean(value))
    setShowAdvanced(hasAdvancedParam)
    void load(nextQuery, nextAdvanced)
  }, [searchParams, load])


  useEffect(() => {
    if (!isLoading && items.length === 0 && query.trim()) {
      setShowAdvanced(true)
    }
  }, [isLoading, items.length, query])

  useEffect(() => {
    const timeout = setTimeout(() => {
      const params = new URLSearchParams()
      if (query.trim()) params.set('q', query.trim())
      Object.entries(advanced).forEach(([key, value]) => {
        if (value?.trim()) {
          params.set(key, value.trim())
        }
      })

      const next = params.toString()
      const current = searchParams.toString()
      if (next !== current) {
        setSearchParams(params, { replace: true })
      }
    }, 300)

    return () => clearTimeout(timeout)
  }, [query, advanced, searchParams, setSearchParams])

  function onSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const params = new URLSearchParams()
    if (query.trim()) params.set('q', query.trim())
    Object.entries(advanced).forEach(([key, value]) => {
      if (value?.trim()) {
        params.set(key, value.trim())
      }
    })
    setSearchParams(params)
  }

  function onChipRemove(chip: SearchChip) {
    const parsed = deriveSearchInput(query, advanced)
    const nextAdvanced = { ...advanced }
    let nextQuery = query

    if (chip.key === 'nace') {
      nextAdvanced.nace = ''
      nextQuery = removeCriterionFromQuery(nextQuery, chip, parsed)
    }
    if (chip.key === 'institution') {
      nextAdvanced.institution = ''
      nextQuery = removeCriterionFromQuery(nextQuery, chip, parsed)
    }
    if (chip.key === 'budget') {
      nextAdvanced.minFunding = ''
      nextAdvanced.maxFunding = ''
      nextQuery = removeCriterionFromQuery(nextQuery, chip, parsed)
    }
    if (chip.key === 'minFunding') {
      nextAdvanced.minFunding = ''
      nextQuery = removeCriterionFromQuery(nextQuery, chip, parsed)
    }
    if (chip.key === 'maxFunding') {
      nextAdvanced.maxFunding = ''
      nextQuery = removeCriterionFromQuery(nextQuery, chip, parsed)
    }
    if (chip.key === 'employees') nextAdvanced.employees = ''
    if (chip.key === 'turnover') nextAdvanced.turnover = ''

    setAdvanced(nextAdvanced)
    setQuery(nextQuery)

    const params = new URLSearchParams()
    if (nextQuery.trim()) params.set('q', nextQuery.trim())
    Object.entries(nextAdvanced).forEach(([key, value]) => {
      if (value?.trim()) params.set(key, value.trim())
    })
    setSearchParams(params)
  }

  return (
    <section className={`page-card ${isPublicRoute ? 'public-grants-shell' : ''}`}>
      <div className="public-grants-head">
        <div>
          <h1>Hibeler</h1>
        </div>
      </div>



      <div className="panel-stats-grid public-stats-grid">
        <button
          type="button"
          className={`panel-stat-card ${quickFilter === 'Acil' ? 'is-active' : ''}`}
          onClick={() => setQuickFilter((prev) => (prev === 'Acil' ? 'ALL' : 'Acil'))}
          style={{ cursor: 'pointer' }}
        >
          <span className="panel-stat-icon is-urgent" aria-hidden="true" />
          <span>Acil</span>
          <strong>{urgencyStats.acil}</strong>
        </button>
        <button
          type="button"
          className={`panel-stat-card ${quickFilter === 'Yaklasiyor' ? 'is-active' : ''}`}
          onClick={() => setQuickFilter((prev) => (prev === 'Yaklasiyor' ? 'ALL' : 'Yaklasiyor'))}
          style={{ cursor: 'pointer' }}
        >
          <span className="panel-stat-icon is-soon" aria-hidden="true" />
          <span>Yaklasiyor</span>
          <strong>{urgencyStats.yaklasiyor}</strong>
        </button>
        <button
          type="button"
          className={`panel-stat-card ${quickFilter === 'Rahat' ? 'is-active' : ''}`}
          onClick={() => setQuickFilter((prev) => (prev === 'Rahat' ? 'ALL' : 'Rahat'))}
          style={{ cursor: 'pointer' }}
        >
          <span className="panel-stat-icon is-relaxed" aria-hidden="true" />
          <span>Rahat</span>
          <strong>{urgencyStats.rahat}</strong>
        </button>
        <button
          type="button"
          className={`panel-stat-card ${quickFilter === 'Kapali' ? 'is-active' : ''}`}
          onClick={() => setQuickFilter((prev) => (prev === 'Kapali' ? 'ALL' : 'Kapali'))}
          style={{ cursor: 'pointer' }}
        >
          <span className="panel-stat-icon is-closed" aria-hidden="true" />
          <span>Kapali</span>
          <strong>{urgencyStats.kapali}</strong>
        </button>
      </div>

      <form onSubmit={onSearchSubmit} className="search-shell-form">
        <div className="search-shell">
          <input
            className="search-shell-input"
            placeholder="Anahtar kelime veya kurum adı ile ara"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <button type="button" className="btn search-shell-advanced" onClick={() => setShowAdvanced((prev) => !prev)}>
            {showAdvanced ? 'Gelişmişi Gizle' : 'Gelişmiş Arama'}
          </button>
          <button type="submit" className="btn btn-primary search-shell-submit" disabled={isLoading}>
            {isLoading ? 'Aranıyor...' : 'Ara'}
          </button>
        </div>

        {parsedPreview.chips.length > 0 ? (
          <div className="search-chip-row">
            {parsedPreview.chips.map((chip, index) => (
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
            <input placeholder="Kurum / Program (örn: KOSGEB)" value={advanced.institution ?? ''} onChange={(event) => setAdvanced((prev) => ({ ...prev, institution: event.target.value }))} />
            <input placeholder="NACE Kodu (örn: 62.01)" value={advanced.nace ?? ''} onChange={(event) => setAdvanced((prev) => ({ ...prev, nace: event.target.value }))} />
            <input type="number" placeholder="Min Bütçe" value={advanced.minFunding ?? ''} onChange={(event) => setAdvanced((prev) => ({ ...prev, minFunding: event.target.value }))} />
            <input type="number" placeholder="Max Bütçe" value={advanced.maxFunding ?? ''} onChange={(event) => setAdvanced((prev) => ({ ...prev, maxFunding: event.target.value }))} />
            <input type="number" placeholder="Çalışan Sayısı" value={advanced.employees ?? ''} onChange={(event) => setAdvanced((prev) => ({ ...prev, employees: event.target.value }))} />
            <input type="number" placeholder="Yıllık Ciro" value={advanced.turnover ?? ''} onChange={(event) => setAdvanced((prev) => ({ ...prev, turnover: event.target.value }))} />
          </div>
        ) : null}
      </form>

      <div className="grant-filter-row">
        {!showPublishedOnly ? (
          <div className="panel-chip-row">
            <button
              type="button"
              className={`btn panel-chip ${statusFilter === 'ALL' ? 'is-active' : ''}`}
              onClick={() => setStatusFilter('ALL')}
            >
              Tum Hibeler
            </button>
            <button
              type="button"
              className={`btn panel-chip ${statusFilter === 'PUBLISHED' ? 'is-active' : ''}`}
              onClick={() => setStatusFilter('PUBLISHED')}
            >
              Yayindaki
            </button>
            <button
              type="button"
              className={`btn panel-chip ${statusFilter === 'CLOSED' ? 'is-active' : ''}`}
              onClick={() => setStatusFilter('CLOSED')}
            >
              Kapali
            </button>
          </div>
        ) : null}


      </div>

      {error ? <p>{error}</p> : null}

      <div id="grant-results" className="public-grants-list">
        {!isLoading && !error && visibleItems.length === 0 ? (
          <p>Sonuç bulunamadı. "nace 62.01" veya sadece "1500000" gibi ifadeleri deneyebilirsiniz.</p>
        ) : null}

        {visibleItems.map((item) => {
          const isClosed = item.status === 'CLOSED' || item.clickable === false
          const eligibility = eligibilityMap[item.id]
          const urgency = getUrgency(item.deadlineAt, item.status)
          const institutionBrand = getInstitutionBrand(item, institutions)
          const recommendationReasons = getRecommendationReasons(item, parsedPreview, urgency)
          const matchCount = getGrantRefinementMatchCount(item, advanced, eligibility)
          const evaluatedRefinementCount = getEvaluatedRefinementCount(advanced, eligibility)
          const compatibilityPercent = evaluatedRefinementCount > 0
            ? Math.round((matchCount / evaluatedRefinementCount) * 100)
            : null

          return (
            <article key={item.id} className={`page-card grant-card-mini ${isPublicRoute ? 'public-grant-card' : ''} ${isClosed ? 'grant-card-frozen' : ''}`}>
              <div className="grant-card-mini-left">
                <div className="grant-card-title-row">
                  {institutionBrand.logoSrc ? (
                    <img className="institution-logo" src={institutionBrand.logoSrc} alt={`${item.providerName ?? 'Kurum'} logosu`} />
                  ) : (
                    <span className="institution-logo-fallback" aria-hidden="true">{institutionBrand.shortCode}</span>
                  )}
                  <h3>{(item.providerName ?? 'Kurum')} - {(item.programName ?? item.title)}</h3>
                </div>
                <p className="grant-card-meta-line"><span className={`urgency-pill ${urgency.className}`}>{urgency.label}</span> {urgency.remainingText}</p>
                <p className="grant-card-subline">Baslangic: {formatDate(item.publishedAt)} | Bitis: {formatDate(item.deadlineAt)}</p>
                <p className="grant-card-summary"><strong>Ozet:</strong> {getCardSummary(item)}</p>
                <p className="grant-card-reason">
                  <strong>Neden onerildi:</strong>{' '}
                  {recommendationReasons.length > 0
                    ? recommendationReasons.join(' • ')
                    : 'Temel arama eslesmesi'}
                </p>
                {compatibilityPercent !== null ? (
                  <p>%{compatibilityPercent} uyumlu • {matchCount}/{evaluatedRefinementCount} kriter</p>
                ) : activeRefinementCount > 0 ? (
                  <p>Uygunluk kuralı eksik olduğu için bu kayıtta kriter skoru hesaplanamadı</p>
                ) : (
                  <p>{item.status === 'CLOSED' ? 'Kapalı Çağrı' : 'Açık Çağrı'}</p>
                )}
              </div>
              <div className="inline-actions grant-card-mini-actions">
                <span className={`grant-status-dot ${isClosed ? 'grant-status-dot-closed' : 'grant-status-dot-open'}`} />
                {isClosed ? (
                  <button type="button" className="btn" disabled>Kapalı</button>
                ) : (
                  <Link className="btn btn-primary" to={`${detailBasePath}/${item.id}`}>Detay</Link>
                )}
              </div>
            </article>
          )
        })}
      </div>


    </section>
  )
}
