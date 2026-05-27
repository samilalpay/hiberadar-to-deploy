import axios from 'axios'
import { env } from '@/shared/config/env'
import { getStoredAuth } from '@/shared/lib/storage'

export const http = axios.create({
  baseURL: env.apiBaseUrl,
})

http.interceptors.request.use((config) => {
  const auth = getStoredAuth()
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})
