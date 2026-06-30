import request from './request'

export function getReportOverview(params) {
  return request.post('/reports/overview', params)
}

export function getEnterpriseActivity(params) {
  return request.post('/reports/enterprise-activity', params)
}

export function getWorkerActivity(params) {
  return request.post('/reports/worker-activity', params)
}

export function getSupplyDemandAnalysis(params) {
  return request.post('/reports/supply-demand', params)
}

export function getConversionFunnel(params) {
  return request.post('/reports/conversion-funnel', params)
}

export function getJobReports(params) {
  return request.post('/job-reports/list', params)
}

export function dismissReport(id) {
  return request.post('/job-reports/dismiss', { id })
}

export function banJob(id) {
  return request.post('/job-reports/ban', { id })
}
