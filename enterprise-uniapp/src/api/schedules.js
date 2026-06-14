import { request } from './request'

export function listScheduleShifts(params = {}) {
  const query = []
  if (params.jobId != null) query.push(`jobId=${params.jobId}`)
  if (params.workerId != null) query.push(`workerId=${params.workerId}`)
  if (params.shiftDate) query.push(`shiftDate=${params.shiftDate}`)
  if (params.status) query.push(`status=${encodeURIComponent(params.status)}`)
  if (params.page != null) query.push(`page=${params.page}`)
  if (params.pageSize != null) query.push(`pageSize=${params.pageSize}`)
  return request('GET', `/schedule-shifts${query.length ? `?${query.join('&')}` : ''}`)
}

export function assignShift(data) {
  return request('POST', '/schedule-shifts', data)
}

export function updateShift(id, data) {
  return request('PUT', `/schedule-shifts?id=${id}`, data)
}

export function deleteShift(id) {
  return request('DELETE', `/schedule-shifts?id=${id}`)
}

export function cancelShift(id) {
  return request('PUT', '/schedule-shifts/cancel', { id })
}
