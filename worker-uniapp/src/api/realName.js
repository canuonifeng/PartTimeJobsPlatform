import request from './request'

export function getRealNameStatus() {
  return request({ url: '/api/worker/real-name', method: 'GET' })
}

export function submitRealName(data) {
  return request({ url: '/api/worker/real-name', method: 'POST', data })
}
