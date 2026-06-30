import request from './request'

export function getBlacklist(params) {
  return request.post('/risk/blacklist', params)
}

export function addToBlacklist(data) {
  return request.post('/risk/blacklist/add', data)
}

export function removeFromBlacklist(id) {
  return request.post('/risk/blacklist/remove', { id })
}

export function getWhitelist(params) {
  return request.post('/risk/whitelist', params)
}

export function getRiskRules() {
  return request.post('/risk/rules')
}

export function toggleRiskRule(id, enabled) {
  return request.post('/risk/rules/toggle', { id, enabled })
}
