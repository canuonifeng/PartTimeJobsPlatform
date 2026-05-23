import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/platform',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(response => response.data, error => {
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
