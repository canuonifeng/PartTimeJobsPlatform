import request from './request'

export function getProfile() {
  return request({
    url: '/profile',
    method: 'GET'
  })
}

export function getProfileDashboard() {
  return request({
    url: '/profile/dashboard',
    method: 'GET'
  })
}

export function updateProfile(data) {
  return request({
    url: '/profile',
    method: 'POST',
    data
  })
}

export function getProfileCompleteness() {
  return request({
    url: '/profile/completeness',
    method: 'GET'
  })
}
