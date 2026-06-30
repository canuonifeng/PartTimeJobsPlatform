import request from './request'

export function listOperators(params) {
  return request.post('/operators/list', params)
}

export function createOperator(data) {
  return request.post('/operators/create', data)
}

export function updateOperator(data) {
  return request.post('/operators/update', data)
}

export function resetOperatorPassword(id) {
  return request.post('/operators/reset-password', { id })
}

export function toggleOperatorStatus(id) {
  return request.post('/operators/toggle-status', { id })
}

export function getRoles() {
  return request.post('/operators/roles')
}

export function listOperationLogs(params) {
  return request.post('/operation-logs/list', params)
}
