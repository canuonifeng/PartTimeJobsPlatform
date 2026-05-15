import request from './request'

export function getJobs(params) {
  return request({
    url: '/api/jobs',
    method: 'GET',
    data: params
  })
}

export function getJobDetail(id) {
  return request({
    url: `/api/jobs/${id}`,
    method: 'GET'
  })
}

export function applyJob(id) {
  return request({
    url: `/api/jobs/${id}/apply`,
    method: 'POST'
  })
}
