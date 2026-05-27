import { useState } from 'react'
import { useInstitutionsQuery, useCreateInstitutionMutation, useUpdateInstitutionMutation, useDeleteInstitutionMutation, useUploadInstitutionLogoMutation } from '../api/institutions.api'
import type { Institution, InstitutionScope } from '@/shared/types/institution'
import { resolveMediaUrl } from '@/shared/lib/media'
import '../../../admin-institutions.css'

export function AdminInstitutionsPage() {
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [formData, setFormData] = useState({
    name: '',
    shortCode: '',
    logoUrl: '',
    scope: 'NATIONAL' as InstitutionScope,
  })
  const [logoFile, setLogoFile] = useState<File | null>(null)
  const [filterScope, setFilterScope] = useState<InstitutionScope | undefined>()

  const { data: institutions = [], isLoading } = useInstitutionsQuery(filterScope)
  const createMutation = useCreateInstitutionMutation()
  const updateMutation = useUpdateInstitutionMutation()
  const deleteMutation = useDeleteInstitutionMutation()
  const uploadLogoMutation = useUploadInstitutionLogoMutation()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    try {
      let savedId = editingId
      if (editingId) {
        await updateMutation.mutateAsync({
          id: editingId,
          data: {
            name: formData.name.trim(),
            shortCode: formData.shortCode.trim(),
            logoUrl: formData.logoUrl.trim() || null,
            scope: formData.scope,
          },
        })
      } else {
        const created = await createMutation.mutateAsync({
          name: formData.name.trim(),
          shortCode: formData.shortCode.trim(),
          logoUrl: formData.logoUrl.trim() || null,
          scope: formData.scope,
        })
        savedId = created?.id ?? null
      }

      if (logoFile && savedId) {
        await uploadLogoMutation.mutateAsync({ id: savedId, file: logoFile })
      }

      setEditingId(null)

      setFormData({ name: '', shortCode: '', logoUrl: '', scope: 'NATIONAL' })
      setLogoFile(null)
      setShowForm(false)
    } catch (error: any) {
      alert(error?.response?.data?.message || 'Hata oluştu')
    }
  }

  const handleEdit = (institution: Institution) => {
    setFormData({
      name: institution.name,
      shortCode: institution.shortCode,
      logoUrl: institution.logoUrl || '',
      scope: institution.scope,
    })
    setEditingId(institution.id)
    setLogoFile(null)
    setShowForm(true)
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Bu kurumu silmek istediğinizden emin misiniz?')) return
    try {
      await deleteMutation.mutateAsync(id)
    } catch (error: any) {
      alert(error?.response?.data?.message || 'Silme sırasında hata oluştu')
    }
  }

  const handleCancel = () => {
    setShowForm(false)
    setEditingId(null)
    setFormData({ name: '', shortCode: '', logoUrl: '', scope: 'NATIONAL' })
    setLogoFile(null)
  }

  return (
    <div className="admin-institutions-page">
      <div className="admin-page-header">
        <h1>Kurumlar Yönetimi</h1>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setShowForm(!showForm)}
        >
          {showForm ? 'İptal' : '+ Yeni Kurum'}
        </button>
      </div>

      {/* Form */}
      {showForm && (
        <div className="admin-form-section">
          <h2>{editingId ? 'Kurumu Düzenle' : 'Yeni Kurum Ekle'}</h2>
          <form onSubmit={handleSubmit} className="admin-form">
            <div className="admin-form-grid">
              <div className="form-group">
                <label htmlFor="name">Kurum Adı *</label>
                <input
                  id="name"
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Örn: KOSGEB"
                />
              </div>

              <div className="form-group">
                <label htmlFor="shortCode">Kısa Kod *</label>
                <input
                  id="shortCode"
                  type="text"
                  required
                  maxLength={10}
                  value={formData.shortCode}
                  onChange={(e) => setFormData({ ...formData, shortCode: e.target.value.toUpperCase() })}
                  placeholder="Örn: KSG"
                />
              </div>

              <div className="form-group">
                <label htmlFor="scope">Kapsam *</label>
                <select
                  id="scope"
                  value={formData.scope}
                  onChange={(e) => setFormData({ ...formData, scope: e.target.value as InstitutionScope })}
                >
                  <option value="NATIONAL">Ulusal</option>
                  <option value="INTERNATIONAL">Uluslararası</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="logoFile">Logo Dosyası</label>
                <input
                  id="logoFile"
                  type="file"
                  accept="image/png,image/jpeg,image/webp,image/svg+xml"
                  onChange={(e) => setLogoFile(e.target.files?.[0] ?? null)}
                />
                <small>PNG, JPG, WEBP veya SVG.</small>
              </div>
            </div>

            <div className="admin-form-actions">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={createMutation.isPending || updateMutation.isPending || uploadLogoMutation.isPending}
              >
                {createMutation.isPending || updateMutation.isPending || uploadLogoMutation.isPending
                  ? 'Kaydediliyor...'
                  : 'Kaydet'}
              </button>
              <button type="button" className="btn btn-ghost" onClick={handleCancel}>
                İptal
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Filter */}
      <div className="admin-filter-section">
        <label>
          <input
            type="radio"
            name="scope"
            value=""
            checked={filterScope === undefined}
            onChange={() => setFilterScope(undefined)}
          />
          Tümü
        </label>
        <label>
          <input
            type="radio"
            name="scope"
            value="NATIONAL"
            checked={filterScope === 'NATIONAL'}
            onChange={() => setFilterScope('NATIONAL')}
          />
          Ulusal
        </label>
        <label>
          <input
            type="radio"
            name="scope"
            value="INTERNATIONAL"
            checked={filterScope === 'INTERNATIONAL'}
            onChange={() => setFilterScope('INTERNATIONAL')}
          />
          Uluslararası
        </label>
      </div>

      {/* Table */}
      <div className="admin-table-section">
        {isLoading ? (
          <p>Yükleniyor...</p>
        ) : institutions.length === 0 ? (
          <p>Kurumu bulunamadı.</p>
        ) : (
          <table className="admin-table">
            <thead>
              <tr>
                <th>Kurum Adı</th>
                <th>Kısa Kod</th>
                <th>Kapsam</th>
                <th>Logo</th>
                <th>İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {institutions.map((inst) => (
                <tr key={inst.id}>
                  <td>{inst.name}</td>
                  <td>{inst.shortCode}</td>
                  <td>{inst.scope === 'NATIONAL' ? 'Ulusal' : 'Uluslararası'}</td>
                  <td>
                    {inst.logoUrl ? (
                      <img src={resolveMediaUrl(inst.logoUrl)} alt={inst.name} className="admin-logo-thumb" />
                    ) : (
                      <span className="admin-logo-placeholder">{inst.shortCode}</span>
                    )}
                  </td>
                  <td className="admin-table-actions">
                    <button
                      type="button"
                      className="btn btn-sm btn-ghost"
                      onClick={() => handleEdit(inst)}
                    >
                      Düzenle
                    </button>
                    <button
                      type="button"
                      className="btn btn-sm btn-ghost btn-danger"
                      onClick={() => handleDelete(inst.id)}
                      disabled={deleteMutation.isPending}
                    >
                      Sil
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
