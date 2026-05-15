import request from './request'

export function getJobReports(params) {
  return request.get('/job-reports', { params })
}

export function getJobReport(id) {
  return request.get('/job-reports', { params: { id } })
}

export function dismissReport(id, data) {
  return request.put('/job-reports/dismiss', data, { params: { id } })
}

export function banJob(id, data) {
  return request.put('/job-reports/ban', data, { params: { id } })
}
