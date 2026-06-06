import request from './request'

export function getSettings() {
  return request({
    url: '/api/settings',
    method: 'GET'
  })
}

export function updateSettings(data) {
  return request({
    url: '/api/settings',
    method: 'PUT',
    data
  })
}
