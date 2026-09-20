import request from './request'

export function getDailySummary(data) {
  return request.post('/finance/daily-summary', data || {})
}

export function getServiceFeeStats(data) {
  return request.post('/finance/service-fee-stats', data || {})
}
