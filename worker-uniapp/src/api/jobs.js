import request from './request'

export function getJobs() {
  return request({
    url: '/api/worker/jobs',
    method: 'GET'
  })
}

export function getJobDetail(id) {
  return request({
    url: '/api/worker/jobs/detail',
    method: 'GET',
    params: { id }
  })
}

export function applyJob(id) {
  return request({
    url: `/api/worker/jobs/apply?id=${id}`,
    method: 'POST'
  })
}
