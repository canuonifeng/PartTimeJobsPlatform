// 修改此处 BASE_URL 为实际部署地址（真机调试需改为电脑局域网IP）
const BASE_URL = 'http://121.199.12.23:8081/api'

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
