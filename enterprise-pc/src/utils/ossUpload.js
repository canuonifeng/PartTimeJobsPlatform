import OSS from 'ali-oss'
import axios from 'axios'
import { getSts } from '../api/upload'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const PUBLIC_BIZS = ['logo', 'job']

function randomFileName(ext) {
  const name = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
  return ext ? `${name}.${ext}` : name
}

function extOf(file) {
  const parts = (file.name || '').split('.')
  return parts.length > 1 ? parts.pop().toLowerCase() : ''
}

function buildPublicUrl(bucket, endpoint, key) {
  const host = String(endpoint).replace(/^https?:\/\//, '').replace(/\/$/, '')
  return `https://${bucket}.${host}/${key}`
}

function authConfig() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function fallbackUpload(file) {
  const formData = new FormData()
  formData.append('file', file)
  const res = await axios.post(`${API_BASE_URL}/enterprise/files/upload`, formData, {
    headers: { ...authConfig() }
  })
  const body = res.data || {}
  const url = body.data?.url || body.url || ''
  return { key: null, url }
}

function isStsUnavailable(body, error) {
  if (body && typeof body.code === 'number') return body.code === 503
  return error?.response?.data?.code === 503
}

export async function uploadToOss(file, biz) {
  let body
  try {
    body = await getSts(biz)
  } catch (e) {
    if (isStsUnavailable(null, e)) {
      return fallbackUpload(file)
    }
    throw e
  }

  if (isStsUnavailable(body)) {
    return fallbackUpload(file)
  }
  if (!body || body.code !== 200 || !body.data) {
    throw new Error(body?.message || '获取上传凭证失败')
  }

  const sts = body.data
  const key = `${sts.prefix}${randomFileName(extOf(file))}`
  const client = new OSS({
    bucket: sts.bucket,
    endpoint: sts.endpoint,
    accessKeyId: sts.accessKeyId,
    accessKeySecret: sts.accessKeySecret,
    stsToken: sts.securityToken,
    secure: true
  })
  await client.put(key, file)

  const url = PUBLIC_BIZS.includes(biz) ? buildPublicUrl(sts.bucket, sts.endpoint, key) : ''
  return { key, url }
}
