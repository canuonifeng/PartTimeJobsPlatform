import request from './request'

export function getSettings() {
  return request({
    url: '/settings',
    method: 'GET'
  })
}

export function updateSettings(data) {
  return request({
    url: '/settings',
    method: 'POST',
    data
  })
}
