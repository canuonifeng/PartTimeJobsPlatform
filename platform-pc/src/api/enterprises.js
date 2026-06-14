import request from './request'

export function listEnterprises(params) {
  return request.post('/enterprises/list', params)
}

export function detailEnterprise(id) {
  return request.post('/enterprises/detail', { id })
}

export function createEnterprise(data) {
  return request.post('/enterprises/create', data)
}

export function updateEnterprise(data) {
  return request.post('/enterprises/update', data)
}

export function suspendEnterprise(id) {
  return request.post('/enterprises/suspend', { id })
}

export function activateEnterprise(id) {
  return request.post('/enterprises/activate', { id })
}

export function listAccounts(enterpriseId) {
  return request.post('/enterprises/accounts/list', { enterpriseId })
}

export function createAccount(data) {
  return request.post('/enterprises/accounts/create', data)
}

export function updateAccount(data) {
  return request.post('/accounts/update', data)
}

export function resetPassword(id, newPassword) {
  return request.post('/accounts/reset-password', { id, newPassword })
}

export function deleteAccount(id) {
  return request.post('/accounts/delete', { id })
}

export function adjustEnterpriseBalance(data) {
  return request.post('/enterprise/balance/adjust', data)
}
