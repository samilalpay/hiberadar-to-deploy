export type InstitutionScope = 'NATIONAL' | 'INTERNATIONAL'

export interface Institution {
  id: number
  name: string
  shortCode: string
  logoUrl: string | null
  scope: InstitutionScope
  createdAt: string
  updatedAt: string | null
}
