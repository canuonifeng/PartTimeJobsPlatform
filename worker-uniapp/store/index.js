import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const workerInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function loadToken() {
    const stored = uni.getStorageSync('token')
    if (stored) {
      token.value = stored
    }
    return stored
  }

  async function wechatLogin() {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          const code = loginRes.code
          try {
            const { data } = await uni.request({
              url: '/api/auth/wechat-login',
              method: 'POST',
              data: { code }
            })
            token.value = data.token
            workerInfo.value = data.worker
            uni.setStorageSync('token', data.token)
            if (data.worker) {
              uni.setStorageSync('workerInfo', JSON.stringify(data.worker))
            }
            resolve(data)
          } catch (err) {
            reject(err)
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }

  async function loadWorkerInfo() {
    if (!token.value) return null
    try {
      const { data } = await uni.request({
        url: '/api/profile',
        method: 'GET',
        header: { Authorization: `Bearer ${token.value}` }
      })
      workerInfo.value = data
      uni.setStorageSync('workerInfo', JSON.stringify(data))
      return data
    } catch {
      return null
    }
  }

  function setWorkerInfo(info) {
    workerInfo.value = info
    uni.setStorageSync('workerInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    workerInfo.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('workerInfo')
    uni.reLaunch({ url: '/pages/login/login' })
  }

  return {
    token,
    workerInfo,
    isLoggedIn,
    loadToken,
    wechatLogin,
    loadWorkerInfo,
    setWorkerInfo,
    logout
  }
})
