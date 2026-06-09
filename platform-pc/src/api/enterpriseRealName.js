import request from './request'

export function listEnterpriseRealName(params) {
  return request.get('/admin/enterprise-real-name', { params })
}

export function approveEnterpriseRealName(id) {
  return request.post('/admin/enterprise-real-name/approve', null, { params: { id } })
}

export function rejectEnterpriseRealName(id, reason) {
  return request.post('/admin/enterprise-real-name/reject', { reason }, { params: { id } })
}
