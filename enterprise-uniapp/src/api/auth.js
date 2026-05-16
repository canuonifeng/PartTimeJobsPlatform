import { request } from './request'

export function login(username, password) {
  return request('POST', '/auth/login', { username, password })
}
