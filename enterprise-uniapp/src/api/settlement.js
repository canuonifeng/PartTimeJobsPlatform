import { request } from './request'

export function listSettlementBills(params = {}) {
  const query = []
  if (params.workerName) query.push(`workerName=${encodeURIComponent(params.workerName)}`)
  if (params.dateFrom) query.push(`dateFrom=${params.dateFrom}`)
  if (params.dateTo) query.push(`dateTo=${params.dateTo}`)
  if (params.page != null) query.push(`page=${params.page}`)
  if (params.pageSize != null) query.push(`pageSize=${params.pageSize}`)
  return request('GET', `/settlement/bills${query.length ? `?${query.join('&')}` : ''}`)
}

export function unsettle(attendanceRecordId) {
  return request('PUT', `/settlement/unsettle/${attendanceRecordId}`)
}
