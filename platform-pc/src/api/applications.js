import request from './request'

export function listApplications(params) {
  return request.post('/applications/list', params)
}

export function detailApplication(id) {
  return request.post('/applications/detail', { id })
}

export function acceptApplication(id) {
  return request.post('/applications/accept', { id })
}

export function rejectApplication(id) {
  return request.post('/applications/reject', { id })
}
