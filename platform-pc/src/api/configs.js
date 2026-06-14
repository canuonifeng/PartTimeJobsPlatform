import request from './request'

export function getConfigs() {
  return request.get('/configs')
}

export function updateConfig(key, value) {
  return request.post('/configs', { key, value })
}
