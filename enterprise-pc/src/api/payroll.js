import request from './request'

export function listBatches(params) {
  return request.get('/payroll/batches', { params })
}

export function getBatch(id) {
  return request.get('/payroll/batches', { params: { id } })
}

export function createBatch(data) {
  return request.post('/payroll/batches', data)
}

export function calculateBatch(id) {
  return request.post('/payroll/batches/calculate', null, { params: { id } })
}

export function confirmBatch(id) {
  return request.post('/payroll/batches/confirm', null, { params: { id } })
}

export function payBatch(id) {
  return request.post('/payroll/batches/pay', null, { params: { id } })
}

export function getBatchItems(id) {
  return request.get('/payroll/batches/items', { params: { id } })
}
