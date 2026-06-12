import { request } from './request'

export function listAccounts(params) {
  return request('POST', '/accounts/list', params)
}

export function createAccount(data) {
  return request('POST', '/accounts/create', data)
}

export function updateAccount(data) {
  return request('POST', '/accounts/update', data)
}

export function resetPassword(data) {
  return request('POST', '/accounts/reset-password', data)
}

export function deleteAccount(id) {
  return request('POST', '/accounts/delete', { id })
}
