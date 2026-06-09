import request from './request'

export function listWorkerRealName(params) {
  return request.get('/admin/worker-real-name', { params })
}

export function approveWorkerRealName(id) {
  return request.post('/admin/worker-real-name/approve', null, { params: { id } })
}

export function rejectWorkerRealName(id, reason) {
  return request.post('/admin/worker-real-name/reject', { reason }, { params: { id } })
}
