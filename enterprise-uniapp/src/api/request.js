const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
const BASE_URL = `${API_BASE_URL}/enterprise`

export function request(method, url, data = null) {
  const token = uni.getStorageSync('token')
  const header = { 'Content-Type': 'application/json' }
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      success: (res) => {
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('user')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('Unauthorized'))
          return
        }
        const body = res.data
        if (body && typeof body.code === 'number') {
          if (body.code === 200) {
            resolve(body.data)
          } else if (body.code === 401) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('user')
            uni.reLaunch({ url: '/pages/login/login' })
            reject(new Error(body.message || 'Unauthorized'))
          } else {
            reject(new Error(body.message || '请求失败'))
          }
        } else {
          resolve(body)
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
