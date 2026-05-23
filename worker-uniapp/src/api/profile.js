import request from './request'

export function getProfile() {
  return request({
    url: '/api/worker/profile',
    method: 'GET'
  })
}

export function updateProfile(data) {
  return request({
    url: '/api/worker/profile',
    method: 'PUT',
    data
  })
}
