import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../api/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const emailSuffix = ref(localStorage.getItem('emailSuffix') || '')

  function isLoggedIn() {
    return !!token.value
  }

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.token
    user.value = res.user || null
    localStorage.setItem('token', res.token)
    if (username && username.includes('@')) {
      const suffix = username.substring(username.indexOf('@') + 1)
      emailSuffix.value = suffix
      localStorage.setItem('emailSuffix', suffix)
    }
    return res
  }

  function logout() {
    token.value = ''
    user.value = null
    emailSuffix.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('emailSuffix')
  }

  return { token, user, emailSuffix, isLoggedIn, login, logout }
})
