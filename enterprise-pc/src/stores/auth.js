import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../api/request'
import { getCurrentAccount } from '../api/account'

function readSavedUser() {
  const savedUser = localStorage.getItem('enterpriseUser')
  if (!savedUser) {
    return null
  }
  try {
    return JSON.parse(savedUser)
  } catch {
    localStorage.removeItem('enterpriseUser')
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(readSavedUser())
  const emailSuffix = ref(localStorage.getItem('emailSuffix') || '')

  function isLoggedIn() {
    return !!token.value
  }

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.token
    localStorage.setItem('token', res.token)
    await loadCurrentUser()
    if (username && username.includes('@')) {
      const suffix = username.substring(username.indexOf('@') + 1)
      emailSuffix.value = suffix
      localStorage.setItem('emailSuffix', suffix)
    }
    return res
  }

  async function loadCurrentUser() {
    if (!token.value) {
      return null
    }
    const currentUser = await getCurrentAccount()
    user.value = currentUser || null
    if (currentUser) {
      localStorage.setItem('enterpriseUser', JSON.stringify(currentUser))
    } else {
      localStorage.removeItem('enterpriseUser')
    }
    return currentUser
  }

  function logout() {
    token.value = ''
    user.value = null
    emailSuffix.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('enterpriseUser')
    localStorage.removeItem('emailSuffix')
  }

  return { token, user, emailSuffix, isLoggedIn, login, loadCurrentUser, logout }
})
