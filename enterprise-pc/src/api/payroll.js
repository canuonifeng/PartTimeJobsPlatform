import request from './request'

export function listBatches(params) {
  return request.get('/payroll-batches', { params })
}

export function getBatch(id) {
  return request.get(`/payroll-batches/${id}`)
}

export function createBatch(data) {
  return request.post('/payroll-batches', data)
}

export function calculateBatch(id) {
  return request.post(`/payroll-batches/${id}/calculate`)
}

export function confirmBatch(id) {
  return request.put(`/payroll-batches/${id}/confirm`)
}

export function payBatch(id) {
  return request.post(`/payroll-batches/${id}/pay`)
}
