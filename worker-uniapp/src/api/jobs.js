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
    url: '/api/jobs/detail',
    method: 'GET',
    data: { id }
  })
}

export function applyJob(id, data) {
  return request({
    url: `/api/jobs/apply?id=${id}`,
    method: 'POST',
    data
  })
}
