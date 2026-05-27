import { useEffect, useState, type FormEvent } from 'react'
import {
  createPreAnalysis,
  listMyPreAnalysis,
  type CreatePreAnalysisPayload,
  type PreAnalysisItem,
} from '@/features/firm/api/pre-analysis.api'

const initialForm: CreatePreAnalysisPayload = {
  activityArea: '',
  machinePark: '',
  investmentPlan: '',
  rdExperience: '',
  exportStatus: '',
  financialCapacity: '',
  note: '',
}

export function FirmPreAnalysisPage() {
  const [form, setForm] = useState<CreatePreAnalysisPayload>(initialForm)
  const [items, setItems] = useState<PreAnalysisItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function load() {
    setIsLoading(true)
    setError('')
    try {
      const page = await listMyPreAnalysis(0, 20)
      setItems(page.content)
    } catch {
      setError('On analiz gecmisi yuklenemedi.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setMessage('')

    if (!form.activityArea?.trim()) {
      setError('Faaliyet alani zorunludur.')
      return
    }

    setIsSaving(true)
    try {
      await createPreAnalysis(form)
      setMessage('On analiz talebiniz olusturuldu.')
      setForm(initialForm)
      await load()
    } catch {
      setError('On analiz talebi olusturulamadi.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className="page-card panel-page-grid">
      <div className="panel-section-head">
        <h1>On Analiz</h1>
        <p>Yeni talep olusturun ve gecmis taleplerinizi takip edin.</p>
      </div>

      <form className="panel-form-grid" onSubmit={onSubmit}>
        <label>
          Faaliyet Alani
          <input value={form.activityArea ?? ''} onChange={(e) => setForm((p) => ({ ...p, activityArea: e.target.value }))} required />
        </label>
        <label>
          Makine Parki
          <input value={form.machinePark ?? ''} onChange={(e) => setForm((p) => ({ ...p, machinePark: e.target.value }))} />
        </label>
        <label>
          Yatirim Plani
          <input value={form.investmentPlan ?? ''} onChange={(e) => setForm((p) => ({ ...p, investmentPlan: e.target.value }))} />
        </label>
        <label>
          AR-GE Deneyimi
          <input value={form.rdExperience ?? ''} onChange={(e) => setForm((p) => ({ ...p, rdExperience: e.target.value }))} />
        </label>
        <label>
          Ihracat Durumu
          <input value={form.exportStatus ?? ''} onChange={(e) => setForm((p) => ({ ...p, exportStatus: e.target.value }))} />
        </label>
        <label>
          Finansal Kapasite
          <input value={form.financialCapacity ?? ''} onChange={(e) => setForm((p) => ({ ...p, financialCapacity: e.target.value }))} />
        </label>
        <label>
          Not
          <input value={form.note ?? ''} onChange={(e) => setForm((p) => ({ ...p, note: e.target.value }))} />
        </label>
        <button type="submit" className="btn btn-primary" disabled={isSaving}>{isSaving ? 'Gonderiliyor...' : 'Talep Olustur'}</button>
      </form>

      {message ? <p className="panel-success">{message}</p> : null}
      {error ? <p className="panel-error">{error}</p> : null}

      <div className="panel-list-grid">
        {isLoading ? <p>Yukleniyor...</p> : null}
        {!isLoading && items.length === 0 ? <p>Henuz on analiz talebiniz yok.</p> : null}

        {items.map((item) => (
          <article key={item.id} className="page-card panel-list-card">
            <h3>Talep #{item.id}</h3>
            <p>Faaliyet Alani: {item.activityArea}</p>
            <p>Durum: <strong>{item.status}</strong></p>
            <p>Gonderim: {item.submittedAt ? new Date(item.submittedAt).toLocaleString('tr-TR') : '-'}</p>
            <p>Inceleme Notu: {item.reviewNote ?? '-'}</p>
            <p>Rapor Ozeti: {item.reportSummary ?? '-'}</p>
          </article>
        ))}
      </div>
    </section>
  )
}
