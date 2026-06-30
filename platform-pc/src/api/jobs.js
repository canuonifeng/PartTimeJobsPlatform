import request from './request'

export function listJobs(params) {
  return request.post('/jobs/list', params)
}

export function detailJob(id) {
  return request.post('/jobs/detail', { id })
}

export function closeJob(id) {
  return request.post('/jobs/close', { id })
}

export function reopenJob(id) {
  return request.post('/jobs/reopen', { id })
}

export function setJobTop(id, isTop) {
  return request.post('/jobs/set-top', { id, isTop })
}

export function setJobRecommended(id, isRecommended) {
  return request.post('/jobs/set-recommended', { id, isRecommended })
}
