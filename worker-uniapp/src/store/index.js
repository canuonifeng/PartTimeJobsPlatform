import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'
import { wechatPhoneLogin as wechatPhoneLoginApi } from '@/api/auth'

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
      return await loadWorkerInfo({ authRedirect: false })
    }

    return null
  }

  async function sendSmsCode(phone) {
    await request({
      url: '/auth/send-code',
      method: 'POST',
      data: { phone }
    })
  }

  async function phoneLogin(phone, code) {
    const data = await request({
      url: '/auth/phone-login',
      method: 'POST',
      data: { phone, code }
    })
    token.value = data.token
    uni.setStorageSync('token', data.token)
    workerInfo.value = null
    return data
  }

  async function wechatLogin() {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          const code = loginRes.code
          try {
            const data = await request({
              url: '/auth/wechat-login',
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

  async function wechatPhoneLogin(phoneAuth = {}) {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          const code = loginRes.code
          try {
            const data = await wechatPhoneLoginApi(code, phoneAuth.phoneCode, phoneAuth.encryptedData, phoneAuth.iv)
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

  async function loadWorkerInfo(options = {}) {
    if (!token.value) return null
    try {
      const data = await request({
        url: '/auth/profile',
        method: 'GET',
        authRedirect: options.authRedirect
      })
      workerInfo.value = data
      uni.setStorageSync('workerInfo', JSON.stringify(data))
      return data
    } catch {
      if (options.authRedirect === false) {
        token.value = ''
        workerInfo.value = null
      }
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
    uni.switchTab({ url: '/pages/jobs/jobList' })
  }

  return {
    token,
    workerInfo,
    isLoggedIn,
    loadSession,
    wechatLogin,
    wechatPhoneLogin,
    phoneLogin,
    sendSmsCode,
    loadWorkerInfo,
    setWorkerInfo,
    logout
  }
})
