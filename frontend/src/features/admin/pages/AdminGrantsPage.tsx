import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { isAxiosError } from 'axios'
import {
  createAdminGrant,
  deleteAdminGrant,
  getAdminGrantDetail,
  listAdminGrants,
  setAdminGrantStatus,
  updateAdminGrant,
  type PageResponse,
} from '@/features/panel/api/panel.api'
import { listGrants as listPublicGrants } from '@/features/public/api/public.api'
import type { GrantDetail, GrantItem } from '@/features/firm/api/grants.api'
import { useInstitutionsQuery } from '../api/institutions.api'

const PAGE_SIZE = 24

function formatDate(value?: string) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleDateString('tr-TR')
}

function formatFunding(min?: number, max?: number, currency?: string) {
  if (min == null && max == null) {
    return '-'
  }

  const unit = currency ?? ''
  if (min != null && max != null) {
    return `${min.toLocaleString('tr-TR')} - ${max.toLocaleString('tr-TR')} ${unit}`.trim()
  }
  if (min != null) {
    return `${min.toLocaleString('tr-TR')}+ ${unit}`.trim()
  }
  return `${max?.toLocaleString('tr-TR')} ${unit}`.trim()
}


type GrantFormState = {
  title: string
  scope: 'NATIONAL' | 'INTERNATIONAL'
  referenceCode: string
  officialUrl: string
  providerName: string
  programName: string
  naceCode: string
  summaryShort: string
  adminQuickInfo: string
  deadlineAt: string
  currency: string
  fundingMin: string
  fundingMax: string
  countryCode: string
}

const EMPTY_FORM: GrantFormState = {
  title: '',
  scope: 'NATIONAL',
  referenceCode: '',
  officialUrl: '',
  providerName: '',
  programName: '',
  naceCode: '',
  summaryShort: '',
  adminQuickInfo: '',
  deadlineAt: '',
  currency: 'TRY',
  fundingMin: '',
  fundingMax: '',
  countryCode: 'TR',
}

function toRequestPayload(form: GrantFormState) {
  return {
    title: form.title,
    scope: form.scope,
    referenceCode: form.referenceCode || undefined,
    officialUrl: form.officialUrl || undefined,
    providerName: form.providerName || undefined,
    programName: form.programName || undefined,
    naceCode: form.naceCode || undefined,
    summaryShort: form.summaryShort || undefined,
    adminQuickInfo: form.adminQuickInfo || undefined,
    deadlineAt: form.deadlineAt || undefined,
    currency: form.currency || undefined,
    fundingMin: form.fundingMin ? Number(form.fundingMin) : undefined,
    fundingMax: form.fundingMax ? Number(form.fundingMax) : undefined,
    countryCode: form.countryCode || undefined,
  }
}

function fromDetail(detail: GrantDetail): GrantFormState {
  return {
    title: detail.title,
    scope: detail.scope ?? 'NATIONAL',
    referenceCode: detail.referenceCode ?? '',
    officialUrl: detail.officialUrl ?? '',
    providerName: detail.providerName ?? '',
    programName: detail.programName ?? '',
    naceCode: detail.naceCode ?? '',
    summaryShort: detail.summaryShort ?? '',
    adminQuickInfo: detail.adminQuickInfo ?? '',
    deadlineAt: detail.deadlineAt ?? '',
    currency: detail.currency ?? 'TRY',
    fundingMin: detail.fundingMin != null ? String(detail.fundingMin) : '',
    fundingMax: detail.fundingMax != null ? String(detail.fundingMax) : '',
    countryCode: detail.countryCode ?? 'TR',
  }
}

function normalizeEmpty(value: string) {
  const trimmed = value.trim()
  return trimmed.length > 0 ? trimmed : '-'
}

export function AdminGrantsPage() {
  const [status, setStatus] = useState<'ALL' | 'DRAFT' | 'PUBLISHED' | 'CLOSED'>('ALL')
  const [programFilter, setProgramFilter] = useState('ALL')
  const [countryFilter, setCountryFilter] = useState('ALL')
  const [quickSearch, setQuickSearch] = useState('')
  const [deadlineSortDir, setDeadlineSortDir] = useState<'NONE' | 'ASC' | 'DESC'>('ASC')
  const [statusSortDir, setStatusSortDir] = useState<'NONE' | 'ASC' | 'DESC'>('NONE')
  const [pageNumber, setPageNumber] = useState(0)
  const [page, setPage] = useState<PageResponse<GrantItem> | null>(null)
  const [programOptions, setProgramOptions] = useState<string[]>([])
  const [countryOptions, setCountryOptions] = useState<string[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isModalLoading, setIsModalLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [isEditOpen, setIsEditOpen] = useState(false)
  const [isDetailOpen, setIsDetailOpen] = useState(false)
  const [isRemoveOpen, setIsRemoveOpen] = useState(false)
  const [activeGrantId, setActiveGrantId] = useState<number | null>(null)
  const [detail, setDetail] = useState<GrantDetail | null>(null)
  const [createForm, setCreateForm] = useState<GrantFormState>(EMPTY_FORM)
  const [editForm, setEditForm] = useState<GrantFormState>(EMPTY_FORM)
  const [totals, setTotals] = useState({ published: 0, closed: 0, draft: 0, total: 0 })
  const { data: institutions = [] } = useInstitutionsQuery()

  useEffect(() => {
    async function loadInitialFilters() {
      try {
        const grantsMeta = await listAdminGrants({ size: 500, page: 0 })
        setProgramOptions(
          [...new Set(grantsMeta.content.map((g) => g.programName?.trim()).filter(Boolean) as string[])].sort(),
        )
        setCountryOptions(
          [...new Set(grantsMeta.content.map((g) => g.countryCode?.trim()).filter(Boolean) as string[])].sort(),
        )
      } catch {
        // Filters are optional.
      }
    }
    void loadInitialFilters()
  }, [])

  useEffect(() => {
    async function loadTotals() {
      try {
        const [published, closed, draft] = await Promise.all([
          listPublicGrants({ status: 'PUBLISHED', page: 0, size: 1 }),
          listPublicGrants({ status: 'CLOSED', page: 0, size: 1 }),
          listAdminGrants({ size: 1, page: 0, status: 'DRAFT' }),
        ])
        setTotals({
          total: published.totalElements + closed.totalElements,
          published: published.totalElements,
          closed: closed.totalElements,
          draft: draft.totalElements,
        })
      } catch {
        setTotals({ published: 0, closed: 0, draft: 0, total: 0 })
      }
    }

    void loadTotals()
  }, [])

  const programQuery = useMemo(() => (programFilter === 'ALL' ? undefined : programFilter), [programFilter])

  async function load(targetPage = pageNumber) {
    setIsLoading(true)
    setError('')
    try {
      const data = await listAdminGrants({
        q: programQuery,
        status: status === 'ALL' ? undefined : status,
        countryCode: countryFilter === 'ALL' ? undefined : countryFilter,
        page: targetPage,
        size: PAGE_SIZE,
      })
      setPage(data)
      setPageNumber(data.number)
    } catch {
      setError('Hibe listesi yuklenemedi.')
    } finally {
      setIsLoading(false)
    }
  }

  async function onStatusChange(grantId: number, next: 'DRAFT' | 'PUBLISHED' | 'CLOSED') {
    try {
      await setAdminGrantStatus(grantId, next)
      setMessage('Hibe durumu guncellendi.')
      await load()
    } catch (err) {
      if (isAxiosError(err)) {
        const code = err.response?.status
        const detailMessage = typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message
        setError(`Hibe durumu guncellenemedi${code ? ` (HTTP ${code})` : ''}${detailMessage ? `: ${detailMessage}` : ''}.`)
      } else {
        setError('Hibe durumu guncellenemedi.')
      }
    }
  }

  async function onCloseGrant(grantId: number) {
    await onStatusChange(grantId, 'CLOSED')
  }

  async function onCreateGrant(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSaving(true)
    setError('')
    setMessage('')
    try {
      await createAdminGrant({
        ...toRequestPayload(createForm),
      })
      setMessage('Yeni hibe olusturuldu.')
      setCreateForm(EMPTY_FORM)
      setIsCreateOpen(false)
      await load()
    } catch (err) {
      if (isAxiosError(err)) {
        const code = err.response?.status
        const detail = typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message
        setError(`Hibe eklenemedi${code ? ` (HTTP ${code})` : ''}${detail ? `: ${detail}` : ''}.`)
      } else {
        setError('Hibe eklenemedi.')
      }
    } finally {
      setIsSaving(false)
    }
  }


  async function openDetailModal(grantId: number) {
    setIsDetailOpen(true)
    setIsModalLoading(true)
    setActiveGrantId(grantId)
    setError('')
    try {
      const grantDetail = await getAdminGrantDetail(grantId)
      setDetail(grantDetail)
    } catch {
      setError('Hibe detayi yuklenemedi.')
      setIsDetailOpen(false)
    } finally {
      setIsModalLoading(false)
    }
  }

  async function openEditModal(grantId: number) {
    setIsEditOpen(true)
    setIsModalLoading(true)
    setActiveGrantId(grantId)
    setError('')
    try {
      const grantDetail = await getAdminGrantDetail(grantId)
      setDetail(grantDetail)
      setEditForm(fromDetail(grantDetail))
    } catch {
      setError('Hibe duzenleme verisi yuklenemedi.')
      setIsEditOpen(false)
    } finally {
      setIsModalLoading(false)
    }
  }

  async function onSaveEdit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeGrantId) {
      return
    }

    setIsSaving(true)
    setError('')
    try {
      await updateAdminGrant(activeGrantId, toRequestPayload(editForm))
      setMessage('Hibe bilgileri guncellendi.')
      setIsEditOpen(false)
      await load()
    } catch (err) {
      if (isAxiosError(err)) {
        const code = err.response?.status
        const detailMessage = typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message
        setError(`Hibe guncellenemedi${code ? ` (HTTP ${code})` : ''}${detailMessage ? `: ${detailMessage}` : ''}.`)
      } else {
        setError('Hibe guncellenemedi.')
      }
    } finally {
      setIsSaving(false)
    }
  }

  async function onDelete(grantId: number) {
    setIsSaving(true)
    setError('')
    try {
      await deleteAdminGrant(grantId)
      setMessage('Hibe silindi.')
      setIsRemoveOpen(false)
      await load()
    } catch (err) {
      if (isAxiosError(err)) {
        const code = err.response?.status
        const detailMessage = typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message
        if (code === 409) {
          setError('Bu hibede basvuru oldugu icin kalici silme yapilamaz. "Kapaliya Al" butonunu kullanin.')
          return
        }

        const detail = detailMessage?.trim().replace(/[.\s]+$/g, '')
        setError(`Hibe silinemedi${code ? ` (HTTP ${code})` : ''}${detail ? `: ${detail}` : ''}.`)
      } else {
        setError('Hibe silinemedi.')
      }
    } finally {
      setIsSaving(false)
    }
  }

  function openRemoveModal(grantId: number) {
    setActiveGrantId(grantId)
    setIsRemoveOpen(true)
  }

  function closeModals() {
    setIsCreateOpen(false)
    setIsEditOpen(false)
    setIsDetailOpen(false)
    setIsRemoveOpen(false)
    setActiveGrantId(null)
    setDetail(null)
    setIsModalLoading(false)
  }

  function statusLabelClass(statusValue?: GrantItem['status']) {
    const key = (statusValue ?? 'DRAFT').toLowerCase()
    return `admin-grant-status is-${key}`
  }

  useEffect(() => {
    void load(pageNumber)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status, pageNumber, programQuery, countryFilter])

  const totalPages = page?.totalPages ?? 0
  const canPrev = pageNumber > 0
  const canNext = pageNumber + 1 < totalPages

  const startPage = Math.max(0, pageNumber - 2)
  const endPage = Math.min(totalPages, startPage + 5)
  const visiblePages = Array.from({ length: Math.max(0, endPage - startPage) }, (_, idx) => startPage + idx)

  const visibleGrants = useMemo(() => {
    const all = [...(page?.content ?? [])]
    const term = quickSearch.trim().toLowerCase()

    const filtered = term
      ? all.filter((grant) => {
        const haystack = [
          grant.title,
          grant.providerName,
          grant.programName,
          grant.referenceCode,
          grant.countryCode,
          grant.naceCode,
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()
        return haystack.includes(term)
      })
      : all

    const statusRank: Record<string, number> = {
      DRAFT: 0,
      PUBLISHED: 1,
      CLOSED: 2,
    }

    filtered.sort((a, b) => {
      if (deadlineSortDir !== 'NONE') {
        const aTime = a.deadlineAt ? new Date(a.deadlineAt).getTime() : Number.POSITIVE_INFINITY
        const bTime = b.deadlineAt ? new Date(b.deadlineAt).getTime() : Number.POSITIVE_INFINITY
        if (aTime !== bTime) {
          return deadlineSortDir === 'ASC' ? aTime - bTime : bTime - aTime
        }
      }

      if (statusSortDir !== 'NONE') {
        const aRank = statusRank[a.status ?? 'DRAFT'] ?? 0
        const bRank = statusRank[b.status ?? 'DRAFT'] ?? 0
        if (aRank !== bRank) {
          return statusSortDir === 'ASC' ? aRank - bRank : bRank - aRank
        }
      }

      return (a.title ?? '').localeCompare(b.title ?? '', 'tr')
    })

    return filtered
  }, [deadlineSortDir, page?.content, quickSearch, statusSortDir])

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>Hibe Yonetimi</h1>
        <p>Listeleme, inceleme, duzenleme ve yayin operasyonlarini tek panelden yonetin.</p>
      </div>

      <div className="admin-grants-hero">
        <div>
          <strong>{page?.totalElements ?? 0} hibe bulundu</strong>
          <p>Detaylari modalda inceleyebilir, panelden cikmadan duzenleyebilirsin.</p>
        </div>
        <div className="panel-chip-row">
          <Link className="btn" to="/admin/grants/public">Hibeler</Link>
          <button type="button" className="btn btn-primary" onClick={() => { setCreateForm(EMPTY_FORM); setIsCreateOpen(true) }}>
            + Yeni Hibe Ekle
          </button>
        </div>
      </div>

      <div className="panel-stats-grid">
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-total" aria-hidden="true" />
          <span>Toplam Hibe</span>
          <strong>{totals.total}</strong>
        </div>
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-open" aria-hidden="true" />
          <span>Yayindaki</span>
          <strong>{totals.published}</strong>
        </div>
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-closed" aria-hidden="true" />
          <span>Kapali</span>
          <strong>{totals.closed}</strong>
        </div>
        <div className="panel-stat-card">
          <span className="panel-stat-icon is-draft" aria-hidden="true" />
          <span>Taslak</span>
          <strong>{totals.draft}</strong>
        </div>
      </div>

      {message ? <p className="panel-success">{message}</p> : null}

      <div className="panel-toolbar">
        <div className="panel-chip-row">
          {(['ALL', 'DRAFT', 'PUBLISHED', 'CLOSED'] as const).map((item) => (
            <button
              key={item}
              type="button"
              className={`btn panel-chip ${status === item ? 'is-active' : ''}`}
              onClick={() => {
                setStatus(item)
                setPageNumber(0)
              }}
            >
              {item}
            </button>
          ))}
        </div>
        <div className="panel-inline-form">
          <select value={programFilter} onChange={(e) => { setProgramFilter(e.target.value); setPageNumber(0) }}>
            <option value="ALL">Tum Programlar</option>
            {programOptions.map((item) => (
              <option key={item} value={item}>{item}</option>
            ))}
          </select>
          <select value={countryFilter} onChange={(e) => { setCountryFilter(e.target.value); setPageNumber(0) }}>
            <option value="ALL">Tum Ulkeler</option>
            {countryOptions.map((item) => (
              <option key={item} value={item}>{item}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="panel-list-head">
        <strong>Toplam Hibe: {page?.totalElements ?? 0}</strong>
        <span>Sayfa {totalPages === 0 ? 0 : pageNumber + 1} / {totalPages}</span>
      </div>

      <div className="admin-card-quickbar">
        <input
          value={quickSearch}
          onChange={(e) => setQuickSearch(e.target.value)}
          placeholder="Kart icinde hizli ara (baslik, kurum, program...)"
        />
        <button
          type="button"
          className="btn"
          onClick={() => {
            setDeadlineSortDir((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'))
            setStatusSortDir('NONE')
          }}
        >
          Son Tarih {deadlineSortDir === 'ASC' ? '↑' : deadlineSortDir === 'DESC' ? '↓' : '↕'}
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => {
            setStatusSortDir((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'))
            setDeadlineSortDir('NONE')
          }}
        >
          Durum {statusSortDir === 'ASC' ? '↑' : statusSortDir === 'DESC' ? '↓' : '↕'}
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => {
            setQuickSearch('')
            setDeadlineSortDir('NONE')
            setStatusSortDir('NONE')
          }}
        >
          Temizle
        </button>
      </div>

      {error ? <p className="panel-error">{error}</p> : null}

      <div className="admin-grant-card-grid">
        {visibleGrants.map((grant) => (
          <article key={grant.id} className="admin-grant-card-modern">
            <div className="admin-grant-card-head">
              <h3 className="text-break">{grant.title}</h3>
              <span className={statusLabelClass(grant.status)}>{grant.status ?? '-'}</span>
            </div>

            <div className="admin-grant-card-meta">
              <p className="text-break"><strong>Kurum:</strong> {grant.providerName ?? '-'}</p>
              <p className="text-break"><strong>Program:</strong> {grant.programName ?? '-'}</p>
              <p className="text-break"><strong>Ref:</strong> {grant.referenceCode ?? '-'}</p>
              <p className="text-break"><strong>Ulke/NACE:</strong> {grant.countryCode ?? '-'} / {grant.naceCode ?? '-'}</p>
              <p><strong>Yayin:</strong> {formatDate(grant.publishedAt)}</p>
              <p><strong>Son Tarih:</strong> {formatDate(grant.deadlineAt)}</p>
              <p className="text-break"><strong>Fon:</strong> {formatFunding(grant.fundingMin, grant.fundingMax, grant.currency)}</p>
            </div>

            <p className="admin-grant-summary text-break">{grant.summaryShort?.trim() || 'Ozet bilgisi bulunmuyor.'}</p>

            <div className="admin-grant-actions">
              <button type="button" className="btn" onClick={() => void openDetailModal(grant.id)}>Detay</button>
              <button type="button" className="btn" onClick={() => void openEditModal(grant.id)}>Duzenle</button>
              <a className="btn" href={`/grants/${grant.id}`} target="_blank" rel="noreferrer">Incele</a>
              <button type="button" className="btn" onClick={() => void onStatusChange(grant.id, 'PUBLISHED')} disabled={grant.status === 'PUBLISHED'}>Yayina Al</button>
              <button type="button" className="btn" onClick={() => void onStatusChange(grant.id, 'DRAFT')} disabled={grant.status === 'DRAFT'}>Taslak</button>
              <button type="button" className="btn" onClick={() => void onCloseGrant(grant.id)} disabled={grant.status === 'CLOSED'}>Kapaliya Al</button>
              <button type="button" className="btn" onClick={() => openRemoveModal(grant.id)}>Sil</button>
            </div>
          </article>
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="panel-pagination">
          <button type="button" className="btn" disabled={!canPrev || isLoading} onClick={() => setPageNumber((p) => Math.max(0, p - 1))}>
            Onceki
          </button>
          {visiblePages.map((idx) => (
            <button
              key={idx}
              type="button"
              className={`btn panel-page-btn ${idx === pageNumber ? 'is-active' : ''}`}
              onClick={() => setPageNumber(idx)}
              disabled={isLoading}
            >
              {idx + 1}
            </button>
          ))}
          <button type="button" className="btn" disabled={!canNext || isLoading} onClick={() => setPageNumber((p) => p + 1)}>
            Sonraki
          </button>
        </div>
      ) : null}

      {(isCreateOpen || isEditOpen || isDetailOpen || isRemoveOpen) ? (
        <div className="panel-modal-backdrop" onClick={closeModals}>
          <div className="panel-modal" onClick={(e) => e.stopPropagation()}>
            <div className="panel-modal-head">
              <h3>
                {isCreateOpen ? 'Yeni Hibe Ekle' : null}
                {isEditOpen ? 'Hibe Duzenle' : null}
                {isDetailOpen ? 'Hibe Detayi' : null}
                {isRemoveOpen ? 'Hibe Sil' : null}
              </h3>
              <button type="button" className="btn" onClick={closeModals}>Kapat</button>
            </div>

            {isModalLoading ? <p>Yukleniyor...</p> : null}

            {isDetailOpen && detail ? (
              <div className="panel-modal-body admin-grant-detail-grid">
                <p className="text-break"><strong>Baslik:</strong> {detail.title}</p>
                <p className="text-break"><strong>Durum:</strong> {detail.status ?? '-'}</p>
                <p className="text-break"><strong>Saglayici:</strong> {detail.providerName ?? '-'}</p>
                <p className="text-break"><strong>Program:</strong> {detail.programName ?? '-'}</p>
                <p className="text-break"><strong>Referans:</strong> {detail.referenceCode ?? '-'}</p>
                <p className="text-break"><strong>Kapsam:</strong> {detail.scope ?? '-'}</p>
                <p className="text-break"><strong>Ulke:</strong> {detail.countryCode ?? '-'}</p>
                <p className="text-break"><strong>NACE:</strong> {detail.naceCode ?? '-'}</p>
                <p className="text-break"><strong>Yayin:</strong> {formatDate(detail.publishedAt)}</p>
                <p className="text-break"><strong>Son Basvuru:</strong> {formatDate(detail.deadlineAt)}</p>
                <p className="text-break"><strong>Fon:</strong> {formatFunding(detail.fundingMin, detail.fundingMax, detail.currency)}</p>
                <p className="panel-form-span-2 text-break"><strong>Resmi URL:</strong> {detail.officialUrl ? <a href={detail.officialUrl} target="_blank" rel="noreferrer">{detail.officialUrl}</a> : '-'}</p>
                <p className="panel-form-span-2 text-break"><strong>Ozet:</strong> {normalizeEmpty(detail.summaryShort ?? '')}</p>
                <p className="panel-form-span-2 text-break"><strong>Admin Notu:</strong> {normalizeEmpty(detail.adminQuickInfo ?? '')}</p>
              </div>
            ) : null}

            {isRemoveOpen ? (
              <form
                className="panel-modal-body panel-form-grid"
                onSubmit={(e) => {
                  e.preventDefault()
                  if (activeGrantId) {
                    void onDelete(activeGrantId)
                  }
                }}
              >
                <p className="panel-form-span-2 text-break">
                  Bu hibe kalici olarak silinecek. Bu islem geri alinmaz.
                </p>
                <div className="panel-chip-row panel-form-span-2">
                  <button type="button" className="btn" disabled={isSaving || !activeGrantId} onClick={() => { if (activeGrantId) { void onCloseGrant(activeGrantId); setIsRemoveOpen(false) } }}>
                    Silmek Yerine Kapaliya Al
                  </button>
                </div>
                <button type="submit" className="btn btn-primary" disabled={isSaving || !activeGrantId}>
                  Kalici Olarak Sil
                </button>
              </form>
            ) : null}

            {isCreateOpen ? (
              <form className="panel-modal-body panel-form-grid" onSubmit={onCreateGrant}>
                <label>
                  Baslik
                  <input value={createForm.title} onChange={(e) => setCreateForm((p) => ({ ...p, title: e.target.value }))} required />
                </label>
                <label>
                  Kapsam
                  <select value={createForm.scope} onChange={(e) => setCreateForm((p) => ({ ...p, scope: e.target.value as 'NATIONAL' | 'INTERNATIONAL' }))}>
                    <option value="NATIONAL">NATIONAL</option>
                    <option value="INTERNATIONAL">INTERNATIONAL</option>
                  </select>
                </label>
                <label>
                  Referans Kodu
                  <input value={createForm.referenceCode} onChange={(e) => setCreateForm((p) => ({ ...p, referenceCode: e.target.value }))} />
                </label>
                <label>
                  Saglayici
                  <select value={createForm.providerName} onChange={(e) => setCreateForm((p) => ({ ...p, providerName: e.target.value }))}>
                    <option value="">Kurum Secin</option>
                    {institutions.map((inst) => (
                      <option key={inst.id} value={inst.name}>
                        {inst.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Program
                  <input value={createForm.programName} onChange={(e) => setCreateForm((p) => ({ ...p, programName: e.target.value }))} />
                </label>
                <label>
                  NACE Kodu
                  <input value={createForm.naceCode} onChange={(e) => setCreateForm((p) => ({ ...p, naceCode: e.target.value }))} />
                </label>
                <label>
                  Resmi URL
                  <input value={createForm.officialUrl} onChange={(e) => setCreateForm((p) => ({ ...p, officialUrl: e.target.value }))} />
                </label>
                <label>
                  Son Basvuru Tarihi
                  <input type="date" value={createForm.deadlineAt} onChange={(e) => setCreateForm((p) => ({ ...p, deadlineAt: e.target.value }))} />
                </label>
                <label>
                  Para Birimi
                  <input value={createForm.currency} maxLength={3} onChange={(e) => setCreateForm((p) => ({ ...p, currency: e.target.value.toUpperCase() }))} />
                </label>
                <label>
                  Minimum Fon
                  <input type="number" min="0" value={createForm.fundingMin} onChange={(e) => setCreateForm((p) => ({ ...p, fundingMin: e.target.value }))} />
                </label>
                <label>
                  Maksimum Fon
                  <input type="number" min="0" value={createForm.fundingMax} onChange={(e) => setCreateForm((p) => ({ ...p, fundingMax: e.target.value }))} />
                </label>
                <label>
                  Ulke Kodu
                  <input value={createForm.countryCode} maxLength={2} onChange={(e) => setCreateForm((p) => ({ ...p, countryCode: e.target.value.toUpperCase() }))} />
                </label>
                <label className="panel-form-span-2">
                  Ozet
                  <textarea value={createForm.summaryShort} onChange={(e) => setCreateForm((p) => ({ ...p, summaryShort: e.target.value }))} rows={3} />
                </label>
                <label className="panel-form-span-2">
                  Admin Notu
                  <textarea value={createForm.adminQuickInfo} onChange={(e) => setCreateForm((p) => ({ ...p, adminQuickInfo: e.target.value }))} rows={3} />
                </label>
                <button type="submit" className="btn btn-primary" disabled={isSaving}>{isSaving ? 'Kaydediliyor...' : 'Olustur'}</button>
              </form>
            ) : null}

            {isEditOpen ? (
              <form className="panel-modal-body panel-form-grid" onSubmit={onSaveEdit}>
                <label>
                  Baslik
                  <input value={editForm.title} onChange={(e) => setEditForm((p) => ({ ...p, title: e.target.value }))} required />
                </label>
                <label>
                  Kapsam
                  <select value={editForm.scope} onChange={(e) => setEditForm((p) => ({ ...p, scope: e.target.value as 'NATIONAL' | 'INTERNATIONAL' }))}>
                    <option value="NATIONAL">NATIONAL</option>
                    <option value="INTERNATIONAL">INTERNATIONAL</option>
                  </select>
                </label>
                <label>
                  Referans Kodu
                  <input value={editForm.referenceCode} onChange={(e) => setEditForm((p) => ({ ...p, referenceCode: e.target.value }))} />
                </label>
                <label>
                  Saglayici
                  <select value={editForm.providerName} onChange={(e) => setEditForm((p) => ({ ...p, providerName: e.target.value }))}>
                    <option value="">Kurum Secin</option>
                    {institutions.map((inst) => (
                      <option key={inst.id} value={inst.name}>
                        {inst.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Program
                  <input value={editForm.programName} onChange={(e) => setEditForm((p) => ({ ...p, programName: e.target.value }))} />
                </label>
                <label>
                  NACE Kodu
                  <input value={editForm.naceCode} onChange={(e) => setEditForm((p) => ({ ...p, naceCode: e.target.value }))} />
                </label>
                <label>
                  Resmi URL
                  <input value={editForm.officialUrl} onChange={(e) => setEditForm((p) => ({ ...p, officialUrl: e.target.value }))} />
                </label>
                <label>
                  Son Basvuru Tarihi
                  <input type="date" value={editForm.deadlineAt} onChange={(e) => setEditForm((p) => ({ ...p, deadlineAt: e.target.value }))} />
                </label>
                <label>
                  Para Birimi
                  <input value={editForm.currency} maxLength={3} onChange={(e) => setEditForm((p) => ({ ...p, currency: e.target.value.toUpperCase() }))} />
                </label>
                <label>
                  Minimum Fon
                  <input type="number" min="0" value={editForm.fundingMin} onChange={(e) => setEditForm((p) => ({ ...p, fundingMin: e.target.value }))} />
                </label>
                <label>
                  Maksimum Fon
                  <input type="number" min="0" value={editForm.fundingMax} onChange={(e) => setEditForm((p) => ({ ...p, fundingMax: e.target.value }))} />
                </label>
                <label>
                  Ulke Kodu
                  <input value={editForm.countryCode} maxLength={2} onChange={(e) => setEditForm((p) => ({ ...p, countryCode: e.target.value.toUpperCase() }))} />
                </label>
                <label className="panel-form-span-2">
                  Ozet
                  <textarea value={editForm.summaryShort} onChange={(e) => setEditForm((p) => ({ ...p, summaryShort: e.target.value }))} rows={3} />
                </label>
                <label className="panel-form-span-2">
                  Admin Notu
                  <textarea value={editForm.adminQuickInfo} onChange={(e) => setEditForm((p) => ({ ...p, adminQuickInfo: e.target.value }))} rows={3} />
                </label>
                <button type="submit" className="btn btn-primary" disabled={isSaving}>{isSaving ? 'Kaydediliyor...' : 'Degisiklikleri Kaydet'}</button>
              </form>
            ) : null}
          </div>
        </div>
      ) : null}
    </section>
  )
}
