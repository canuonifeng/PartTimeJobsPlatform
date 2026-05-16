import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(null)

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
  }

  function logout() {
    token.value = ''
    user.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('user')
    uni.reLaunch({ url: '/pages/login/login' })
  }

  return { token, user, isLoggedIn, companyId, setToken, setUser, init, logout }
})
