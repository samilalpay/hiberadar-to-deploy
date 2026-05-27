import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { http } from '@/shared/lib/http'
import type { Institution, InstitutionScope } from '@/shared/types/institution'


export interface CreateInstitutionRequest {
  name: string
  shortCode: string
  logoUrl: string | null
  scope: InstitutionScope
}

// --- Queries ---
export const useInstitutionsQuery = (scope?: InstitutionScope) => {
  return useQuery<Institution[]>({
    queryKey: ['institutions', scope],
    queryFn: async () => {
      const params = new URLSearchParams()
      if (scope) params.append('scope', scope)
      const response = await http.get(`/api/institutions?${params.toString()}`)
      return response.data || []
    },
  })
}

export const useInstitutionQuery = (id: number) => {
  return useQuery<Institution>({
    queryKey: ['institution', id],
    queryFn: async () => {
      const response = await http.get(`/api/institutions/${id}`)
      return response.data
    },
    enabled: !!id,
  })
}

// --- Mutations ---
export const useCreateInstitutionMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (data: CreateInstitutionRequest) => {
      const response = await http.post('/api/admin/institutions', data)
      return response.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['institutions'] })
    },
  })
}

export const useUpdateInstitutionMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: CreateInstitutionRequest }) => {
      const response = await http.put(`/api/admin/institutions/${id}`, data)
      return response.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['institutions'] })
    },
  })
}

export const useDeleteInstitutionMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => {
      await http.delete(`/api/admin/institutions/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['institutions'] })
    },
  })
}

export const useUploadInstitutionLogoMutation = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, file }: { id: number; file: File }) => {
      const formData = new FormData()
      formData.append('file', file)
      const response = await http.post(`/api/admin/institutions/${id}/logo`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      return response.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['institutions'] })
    },
  })
}
