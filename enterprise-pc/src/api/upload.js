import axios from 'axios'
import request from './request'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export const enterpriseUploadUrl = `${API_BASE_URL}/enterprise/files/upload`

export function getUploadHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function getUploadUrl(response) {
  return response?.data?.url || response?.url || ''
}

function authConfig() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getSts(biz) {
  const res = await axios.post(`${API_BASE_URL}/enterprise/files/sts`, null, {
    params: { biz },
    headers: authConfig()
  })
  return res.data
}

export function getSignedUrl(key) {
  return request.get('/files/signed-url', { params: { key } })
}
