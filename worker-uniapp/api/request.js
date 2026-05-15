const BASE_URL = 'https://api.example.com'

function request(config) {
  const token = uni.getStorageSync('token')
  const header = { ...config.header }

  if (token) {
    header.Authorization = `Bearer ${token}`
  }

  if (config.dataType !== 'json' && !(config.data instanceof FormData)) {
    header['Content-Type'] = 'application/json'
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: (config.baseURL || BASE_URL) + config.url,
      method: config.method || 'GET',
      data: config.data,
      header,
      dataType: config.dataType || 'json',
      timeout: config.timeout || 15000,
      success: (res) => {
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('workerInfo')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          reject(new Error(res.data?.message || `请求失败(${res.statusCode})`))
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '网络异常'))
      }
    })
  })
}

export default request
