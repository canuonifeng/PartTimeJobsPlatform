import request from './request'

export function getRealName() {
  return request.get('/enterprise/real-name')
}

export function submitRealName(data) {
  return request.post('/enterprise/real-name', data)
}
