import request from './request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

export function getDashboardTrend(days) {
  return request.post('/dashboard/trend', { days: days || 7 })
}
