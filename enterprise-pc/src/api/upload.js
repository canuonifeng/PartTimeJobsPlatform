const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export const enterpriseUploadUrl = `${API_BASE_URL}/enterprise/files/upload`

export function getUploadHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function getUploadUrl(response) {
  return response?.data?.url || response?.url || ''
}
