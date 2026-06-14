import request from './request'

export function getJobReports(params) {
  return request.get('/job-reports', { params })
}

export function getJobReport(id) {
  return request.get('/job-reports', { params: { id } })
}

export function dismissReport(id, data) {
  return request.post('/job-reports/dismiss', { ...data, id })
}

export function banJob(id, data) {
  return request.post('/job-reports/ban', { ...data, id })
}
