import request from './request'
import { openLoginSheet } from '@/utils/loginSheet'

const BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api/worker`

export function getRealNameStatus() {
  return request({ url: '/real-name', method: 'GET' })
}

export function submitRealName(data) {
  return request({ url: '/real-name', method: 'POST', data })
}

export function uploadRealNameImage(filePath) {
  const token = uni.getStorageSync('token')
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}/files/upload`,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('workerInfo')
          openLoginSheet()
          reject(new Error('登录已过期'))
          return
        }
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (body?.code === 200) {
            resolve(body.data)
          } else if (body?.code === 401) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('workerInfo')
            openLoginSheet()
            reject(new Error(body?.message || '登录已过期'))
          } else {
            reject(new Error(body?.message || '上传失败'))
          }
        } catch {
          reject(new Error('上传响应解析失败'))
        }
      },
      fail: (err) => reject(new Error(err?.errMsg || '上传失败'))
    })
  })
}
