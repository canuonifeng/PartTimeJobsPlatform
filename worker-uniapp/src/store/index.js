import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const workerInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  function readStoredWorkerInfo() {
    const stored = uni.getStorageSync('workerInfo')
    if (!stored) return null
    if (typeof stored === 'object') return stored
    try {
      return JSON.parse(stored)
    } catch {
      return null
    }
  }

  async function loadSession() {
    const storedToken = uni.getStorageSync('token')
    if (storedToken) {
      token.value = storedToken
    }

    const storedWorkerInfo = readStoredWorkerInfo()
    if (storedWorkerInfo) {
      workerInfo.value = storedWorkerInfo
      return storedWorkerInfo
    }

    if (storedToken) {
      return await loadWorkerInfo()
    }

    return null
  }

  async function wechatLogin() {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          const code = loginRes.code
          try {
            const data = await request({
              url: '/api/auth/wechat-login',
              method: 'POST',
              data: { code }
            })
            token.value = data.token
            uni.setStorageSync('token', data.token)
            workerInfo.value = null
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
      const data = await request({
        url: '/api/auth/profile',
        method: 'GET'
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
    loadSession,
    wechatLogin,
    loadWorkerInfo,
    setWorkerInfo,
    logout
  }
})
