import request from './request'

export function getRealNameStatus() {
  return request({ url: '/real-name', method: 'GET' })
}

export function submitRealName(data) {
  return request({ url: '/real-name', method: 'POST', data })
}
