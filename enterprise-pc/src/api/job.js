import request from './request'

export function listJobs(params) {
  return request.get('/jobs', { params })
}

export function getJob(id) {
  return request.get(`/jobs/${id}`)
}

export function createJob(data) {
  return request.post('/jobs', data)
}

export function updateJob(id, data) {
  return request.put(`/jobs/${id}`, data)
}

export function deleteJob(id) {
  return request.delete(`/jobs/${id}`)
}

export function publishJob(id) {
  return request.put(`/jobs/${id}/publish`)
}

export function closeJob(id) {
  return request.put(`/jobs/${id}/close`)
}

export function reopenJob(id) {
  return request.put(`/jobs/${id}/reopen`)
}
