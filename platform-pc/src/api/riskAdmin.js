import request from './request'

export function getRiskMonitor() {
  return request.post('/risk/monitor', {})
}

export function getBlacklist(params) {
  return request.post('/risk/blacklist', params || {})
}

export function addToBlacklist(data) {
  return request.post('/risk/blacklist/add', data)
}

export function removeFromBlacklist(data) {
  return request.post('/risk/blacklist/remove', data)
}

export function getWhitelist(params) {
  return request.post('/risk/whitelist', params || {})
}

export function addToWhitelist(data) {
  return request.post('/risk/whitelist/add', data)
}

export function removeFromWhitelist(data) {
  return request.post('/risk/whitelist/remove', data)
}

export function getRiskRules() {
  return request.post('/risk/rules', {})
}

export function toggleRiskRule(data) {
  return request.post('/risk/rules/toggle', data)
}

export function saveRiskRule(data) {
  return request.post('/risk/rules/save', data)
}
