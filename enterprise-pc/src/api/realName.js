import request from './request'

export function getRealName() {
  return request.get('/real-name')
}

export function submitRealName(data) {
  return request.post('/real-name', data)
}
