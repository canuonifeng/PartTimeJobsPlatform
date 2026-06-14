import request from './request'

export function getConfigs() {
  return request.get('/admin/configs')
}

export function updateConfig(key, value) {
  return request.post('/admin/configs', { key, value })
}
