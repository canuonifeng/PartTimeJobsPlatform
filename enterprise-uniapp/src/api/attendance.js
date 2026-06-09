import { request } from './request'

export function listAttendance(params = {}) {
  const query = []
  if (params.workerName) query.push(`workerName=${encodeURIComponent(params.workerName)}`)
  if (params.dateFrom) query.push(`dateFrom=${params.dateFrom}`)
  if (params.dateTo) query.push(`dateTo=${params.dateTo}`)
  if (params.settlementStatus) query.push(`settlementStatus=${params.settlementStatus}`)
  if (params.page != null) query.push(`page=${params.page}`)
  if (params.pageSize != null) query.push(`pageSize=${params.pageSize}`)
  return request('GET', `/attendance/hours${query.length ? `?${query.join('&')}` : ''}`)
}

export function updateAttendance(id, data) {
  return request('PUT', `/attendance/hours?id=${id}`, data)
}

export function batchPay(ids) {
  return request('PUT', '/attendance/hours/pay', ids)
}

export function batchDeleteAttendance(ids) {
  return request('DELETE', '/attendance/hours', ids)
}
