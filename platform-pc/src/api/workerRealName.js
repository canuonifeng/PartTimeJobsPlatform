import request from './request'

export function listWorkerRealName(params) {
  return request.get('/admin/worker-real-name', { params })
}

export function approveWorkerRealName(id) {
  return request.post(`/admin/worker-real-name/${id}/approve`)
}

export function rejectWorkerRealName(id, reason) {
  return request.post(`/admin/worker-real-name/${id}/reject`, { reason })
}
