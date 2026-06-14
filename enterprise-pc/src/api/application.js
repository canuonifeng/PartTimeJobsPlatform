import request from './request'

export function listApplications(params) {
  return request.get('/applications', { params })
}

export function getApplication(id) {
  return request.get('/applications', { params: { id } })
}

export function acceptApplication(applicationId) {
  return request.post('/applications/accept', { applicationId })
}

export function rejectApplication(applicationId) {
  return request.post('/applications/reject', { applicationId })
}
