const BASE_URL = import.meta.env.DEV ? 'http://localhost:8081/api' : 'http://121.199.12.23:8081/api'

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
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
