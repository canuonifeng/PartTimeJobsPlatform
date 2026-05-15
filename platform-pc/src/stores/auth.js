import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../api/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.token
    user.value = res.user
    localStorage.setItem('token', res.token)
    return res
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  function isAuthenticated() {
    return !!token.value
  }

  return { token, user, login, logout, isAuthenticated }
})
