import { getSts } from '@/api/file'

// #ifdef H5
import OSS from 'ali-oss'
// #endif

// #ifdef MP-WEIXIN
import CryptoJS from 'crypto-js'
// #endif

const BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api/worker`

function guessExt(filePath) {
  const m = String(filePath || '').match(/\.(\w+)(\?.*)?$/)
  const ext = (m && m[1] || 'jpg').toLowerCase()
  if (ext === 'png') return 'png'
  return 'jpg'
}

function buildObjectKey(prefix, filePath) {
  const ext = guessExt(filePath)
  const fileName = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}.${ext}`
  const p = String(prefix || '').replace(/^\/+/, '')
  return p.endsWith('/') ? p + fileName : `${p}/${fileName}`
}

function normalizeEndpoint(endpoint) {
  return String(endpoint || '').replace(/^https?:\/\//, '').replace(/\/+$/, '')
}

function buildPublicUrl(sts, key) {
  return `https://${sts.bucket}.${normalizeEndpoint(sts.endpoint)}/${key}`
}

// #ifdef H5
function extractRegion(endpoint) {
  const host = normalizeEndpoint(endpoint)
  return host.replace(/\.aliyuncs\.com.*$/, '') || host
}

async function putToOss(sts, key, filePath) {
  const client = new OSS({
    region: extractRegion(sts.endpoint),
    accessKeyId: sts.accessKeyId,
    accessKeySecret: sts.accessKeySecret,
    stsToken: sts.securityToken,
    bucket: sts.bucket
  })
  const blob = await (await fetch(filePath)).blob()
  await client.put(key, blob)
}
// #endif

// #ifdef MP-WEIXIN
function guessContentType(filePath) {
  return guessExt(filePath) === 'png' ? 'image/png' : 'image/jpeg'
}

function putToOss(sts, key, filePath) {
  const host = normalizeEndpoint(sts.endpoint)
  const url = `https://${sts.bucket}.${host}/${encodeURI(key)}`
  const fs = uni.getFileSystemManager()
  const data = fs.readFileSync(filePath)
  const contentType = guessContentType(filePath)
  const date = new Date().toUTCString()
  const stringToSign = [
    'PUT',
    '',
    contentType,
    date,
    `x-oss-security-token:${sts.securityToken}`,
    `/${sts.bucket}/${key}`
  ].join('\n')
  const signature = CryptoJS.enc.Base64.stringify(
    CryptoJS.HmacSHA1(stringToSign, sts.accessKeySecret)
  )
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: 'PUT',
      data,
      responseType: 'arraybuffer',
      header: {
        Authorization: `OSS ${sts.accessKeyId}:${signature}`,
        'Content-Type': contentType,
        'x-oss-security-token': sts.securityToken,
        Date: date
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res)
        } else {
          reject(new Error('OSS 上传失败(' + res.statusCode + ')'))
        }
      },
      fail: (err) => reject(new Error(err?.errMsg || 'OSS 上传失败'))
    })
  })
}
// #endif

function legacyUpload(filePath) {
  const token = uni.getStorageSync('token')
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}/files/upload`,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (res.statusCode === 200 && body && body.code === 200) {
            resolve({ key: '', url: (body.data && body.data.url) || '' })
          } else {
            reject(new Error((body && body.message) || '上传失败'))
          }
        } catch {
          reject(new Error('上传响应解析失败'))
        }
      },
      fail: (err) => reject(new Error(err?.errMsg || '上传失败'))
    })
  })
}

// 上传图片到 OSS 直传链路。
// biz: realname（私有，返回对象 key）/ avatar（公有，返回完整公开 URL）
// 返回 { key, url }：key 为 OSS 对象 key；url 仅 avatar 公有桶场景可用。
// STS 未配置（后端 503）或请求异常时，自动回退旧 /files/upload 中转（dev 可用）。
export async function uploadToOss(filePath, biz) {
  let sts
  try {
    sts = await getSts(biz)
  } catch (e) {
    return legacyUpload(filePath)
  }
  const key = buildObjectKey(sts.prefix, filePath)
  await putToOss(sts, key, filePath)
  const result = { key, url: '' }
  if (biz === 'avatar') {
    result.url = buildPublicUrl(sts, key)
  }
  return result
}
