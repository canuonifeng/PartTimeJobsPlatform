import request from './request'

export function listApplications(params) {
  return request.get('/applications', { params })
}

export function getApplication(id) {
  return request.get('/applications', { params: { id } })
}

export function acceptApplication(applicationId) {
  return request.put('/applications/accept', null, { params: { applicationId } })
}

export function rejectApplication(applicationId) {
  return request.put('/applications/reject', null, { params: { applicationId } })
}
