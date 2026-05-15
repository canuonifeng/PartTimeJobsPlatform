import request from './request'

export function listApplications(params) {
  return request.get('/applications', { params })
}

export function getApplication(id) {
  return request.get(`/applications/${id}`)
}

export function acceptApplication(id) {
  return request.put(`/applications/${id}/accept`)
}

export function rejectApplication(id) {
  return request.put(`/applications/${id}/reject`)
}
