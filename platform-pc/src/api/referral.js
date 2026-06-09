import request from './request'

export function getConfig() {
  return request.get('/referral/config')
}

export function updateConfig(configs) {
  return request.put('/referral/config', configs)
}

export function getAuditList(params) {
  return request.get('/referral/audit/list', { params })
}

export function approveReward(id, remark) {
  return request.post(`/referral/audit/${id}/approve`, { remark })
}

export function rejectReward(id, remark) {
  return request.post(`/referral/audit/${id}/reject`, { remark })
}
