import request from './request'

export function listBatches(jobId) {
  return request.post('/annotation-batches/list', { jobId })
}

export function createBatch(data) {
  return request.post('/annotation-batches', data)
}

export function updateBatch(data) {
  return request.post('/annotation-batches/update', data)
}

export function toggleBatch(data) {
  return request.post('/annotation-batches/toggle', data)
}
