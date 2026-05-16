import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../api/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  function isLoggedIn() {
    return !!token.value
  }

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.token
    user.value = res.user || null
    localStorage.setItem('token', res.token)
    return res
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, isLoggedIn, login, logout }
})
