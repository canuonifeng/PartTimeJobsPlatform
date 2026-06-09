import { request } from './request'

export function getRealName() {
  return request('GET', '/enterprise/real-name')
}

export function submitRealName(data) {
  return request('POST', '/enterprise/real-name', data)
}
