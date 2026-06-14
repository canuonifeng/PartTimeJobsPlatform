import request from './request'

export function listAccounts(params) {
  return request.post('/accounts/list', params)
}

export function createAccount(data) {
  return request.post('/accounts/create', data)
}

export function updateAccount(data) {
  return request.post('/accounts/update', data)
}

export function resetPassword(data) {
  return request.post('/accounts/reset-password', data)
}

export function updateCurrentPassword(data) {
  return request.post('/accounts/me/password', data)
}

export function deleteAccount(id) {
  return request.post('/accounts/delete', { id })
}
