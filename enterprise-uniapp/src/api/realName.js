import { request } from './request'

export function getRealName() {
  return request('GET', '/real-name')
}

export function submitRealName(data) {
  return request('POST', '/real-name', data)
}
