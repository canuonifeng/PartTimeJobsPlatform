import request from './request'

export function getProfile() {
  return request({
    url: '/api/profile',
    method: 'GET'
  })
}

export function getProfileDashboard() {
  return request({
    url: '/api/profile/dashboard',
    method: 'GET'
  })
}

export function updateProfile(data) {
  return request({
    url: '/api/profile',
    method: 'POST',
    data
  })
}

export function getProfileCompleteness() {
  return request({
    url: '/api/profile/completeness',
    method: 'GET'
  })
}
