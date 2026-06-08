import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(response => {
  const body = response.data
  if (body && typeof body.code === 'number') {
    if (body.code === 200) {
      return body.data
    } else if (body.code === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(new Error(body.message || '登录已过期'))
    } else {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
  }
  return body
}, error => {
  if (error.response) {
    if (error.response.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response.data?.message || '请求失败')
    }
  } else {
    ElMessage.error('网络错误')
  }
  return Promise.reject(error)
})

export default request
