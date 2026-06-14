const BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api/worker`

function cleanParams(obj) {
  const result = {}
  for (const k in obj) {
    if (obj[k] !== undefined && obj[k] !== null && obj[k] !== '') {
      result[k] = obj[k]
    }
  }
  return result
}

function request(config) {
  const token = uni.getStorageSync('token')
  const header = { 'Content-Type': 'application/json' }

  if (token) {
    header.Authorization = 'Bearer ' + token
  }

  return new Promise(function (resolve, reject) {
    uni.request({
      url: (config.baseURL || BASE_URL) + config.url,
      method: config.method || 'GET',
      data: config.data ? cleanParams(config.data) : undefined,
      header: header,
      dataType: 'json',
      timeout: 15000,
      success: function (res) {
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('workerInfo')
          const pages = getCurrentPages()
          const currentPage = pages[pages.length - 1]
          const route = currentPage?.route ? `/${currentPage.route}` : ''
          const options = currentPage?.options || {}
          const query = Object.entries(options)
            .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
            .join('&')
          const redirect = route ? `${route}${query ? `?${query}` : ''}` : '/pages/index/index'
          if (route !== '/pages/login/login') {
            uni.reLaunch({ url: `/pages/login/login?redirect=${encodeURIComponent(redirect)}` })
          }
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data
          if (body && typeof body.code === 'number') {
            if (body.code === 200) {
              resolve(body.data)
            } else if (body.code === 401) {
              uni.removeStorageSync('token')
              uni.removeStorageSync('workerInfo')
              uni.reLaunch({ url: '/pages/login/login' })
              reject(new Error(body.message || '登录已过期'))
            } else {
              reject(new Error(body.message || '请求失败'))
            }
          } else {
            resolve(body)
          }
        } else {
          reject(new Error((res.data && res.data.message) || ('请求失败(' + res.statusCode + ')')))
        }
      },
      fail: function (err) {
        reject(new Error(err.errMsg || '网络异常'))
      }
    })
  })
}

export default request
