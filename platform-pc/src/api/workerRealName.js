import request from './request'

export function listWorkerRealName(params) {
  return request.get('/worker-real-name', { params })
}

export function approveWorkerRealName(id) {
  return request.post('/worker-real-name/approve', { id })
}

export function rejectWorkerRealName(id, reason) {
  return request.post('/worker-real-name/reject', { id, reason })
}
