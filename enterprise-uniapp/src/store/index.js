import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(null)
  const displayName = ref('')
  const emailSuffix = ref('')

  const isLoggedIn = computed(() => !!token.value)
  const companyId = computed(() => user.value?.companyId || '')

  function setToken(newToken) {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }

  function setUser(userInfo) {
    user.value = userInfo
    if (userInfo) {
      uni.setStorageSync('user', JSON.stringify(userInfo))
    }
  }

  function setDisplayName(name) {
    displayName.value = name
    uni.setStorageSync('displayName', name)
  }

  function setEmailSuffix(suffix) {
    emailSuffix.value = suffix
    uni.setStorageSync('emailSuffix', suffix)
  }

  function init() {
    const savedToken = uni.getStorageSync('token')
    if (savedToken) {
      token.value = savedToken
    }
    const savedUser = uni.getStorageSync('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch {
        user.value = null
      }
    }
    displayName.value = uni.getStorageSync('displayName') || ''
    emailSuffix.value = uni.getStorageSync('emailSuffix') || ''
  }

  function logout() {
    token.value = ''
    user.value = null
    displayName.value = ''
    emailSuffix.value = ''
    uni.removeStorageSync('token')
    uni.removeStorageSync('user')
    uni.removeStorageSync('displayName')
    uni.removeStorageSync('emailSuffix')
    uni.reLaunch({ url: '/pages/login/login' })
  }

  return { token, user, displayName, emailSuffix, isLoggedIn, companyId, setToken, setUser, setDisplayName, setEmailSuffix, init, logout }
})
