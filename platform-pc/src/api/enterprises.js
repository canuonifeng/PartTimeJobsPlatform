import request from './request'

export function listEnterprises(params) {
  return request.post('/admin/enterprises/list', params)
}

export function detailEnterprise(id) {
  return request.post('/admin/enterprises/detail', { id })
}

export function createEnterprise(data) {
  return request.post('/admin/enterprises/create', data)
}

export function updateEnterprise(data) {
  return request.post('/admin/enterprises/update', data)
}

export function suspendEnterprise(id) {
  return request.post('/admin/enterprises/suspend', { id })
}

export function activateEnterprise(id) {
  return request.post('/admin/enterprises/activate', { id })
}

export function listAccounts(enterpriseId) {
  return request.post('/admin/enterprises/accounts/list', { enterpriseId })
}

export function createAccount(data) {
  return request.post('/admin/enterprises/accounts/create', data)
}

export function updateAccount(data) {
  return request.post('/admin/accounts/update', data)
}

export function resetPassword(id, newPassword) {
  return request.post('/admin/accounts/reset-password', { id, newPassword })
}

export function deleteAccount(id) {
  return request.post('/admin/accounts/delete', { id })
}
