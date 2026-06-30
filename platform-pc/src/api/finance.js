import request from './request'

export function getDailySummary(params) {
  return request.post('/finance/daily-summary', params)
}

export function getServiceFeeStats(params) {
  return request.post('/finance/service-fee-stats', params)
}
