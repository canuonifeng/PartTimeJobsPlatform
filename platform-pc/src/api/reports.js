import request from './request'

export function getJobReports() {
  return request.get('/job-reports')
}

export function dismissReport(id) {
  return request.put(`/job-reports/${id}/dismiss`)
}

export function banJob(id) {
  return request.put(`/job-reports/${id}/ban`)
}
