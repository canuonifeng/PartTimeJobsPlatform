import request from './request'

export function getJobs(params) {
  return request({
    url: '/jobs',
    method: 'GET',
    data: params
  })
}

export function getCategories() {
  return request({
    url: '/job-categories',
    method: 'GET'
  })
}

export function getJobDetail(id) {
  return request({
    url: '/jobs/detail',
    method: 'GET',
    data: { id }
  })
}

export function applyJob(id, data) {
  return request({
    url: '/jobs/apply',
    method: 'POST',
    data: { ...data, jobId: id }
  })
}

export function getMySignups(params) {
  return request({
    url: '/jobs/applications/my',
    method: 'GET',
    data: params
  })
}
