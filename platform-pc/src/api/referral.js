import request from './request'

export function getConfig() {
  return request.get('/referral/config')
}

export function updateConfig(configs) {
  return request.post('/referral/config', { configs })
}

export function getAuditList(params) {
  return request.get('/referral/audit/list', { params })
}

export function approveReward(id, remark) {
  return request.post('/referral/audit/approve', { id, remark })
}

export function rejectReward(id, remark) {
  return request.post('/referral/audit/reject', { id, remark })
}
