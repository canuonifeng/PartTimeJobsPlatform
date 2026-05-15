import request from './request'

export function getConfigs() {
  return request.get('/admin/configs')
}

export function updateConfig(key, value) {
  return request.put('/admin/configs', { value }, { params: { key } })
}
