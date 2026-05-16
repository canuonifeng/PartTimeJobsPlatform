// 修改此处 BASE_URL 为实际部署地址（真机调试需改为电脑局域网IP）
const BASE_URL = 'http://localhost:8082'

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
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
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
