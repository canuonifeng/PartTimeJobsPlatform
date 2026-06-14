import request from './request'

export function getJobs(params) {
  return request({
    url: '/api/jobs',
    method: 'GET',
    data: params
  })
}

export function getCategories() {
  return request({
    url: '/api/job-categories',
    method: 'GET'
  })
}

export function getJobDetail(id) {
  return request({
    url: '/api/jobs/detail',
    method: 'GET',
    data: { id }
  })
}

export function applyJob(id, data) {
  return request({
    url: '/api/jobs/apply',
    method: 'POST',
    data: { ...data, jobId: id }
  })
}

export function getMySignups(params) {
  return request({
    url: '/api/jobs/applications/my',
    method: 'GET',
    data: params
  })
}
