import request from './request'

export function listEnterpriseRealName(params) {
  return request.get('/enterprise-real-name', { params })
}

export function approveEnterpriseRealName(id) {
  return request.post('/enterprise-real-name/approve', { id })
}

export function rejectEnterpriseRealName(id, reason) {
  return request.post('/enterprise-real-name/reject', { id, reason })
}
