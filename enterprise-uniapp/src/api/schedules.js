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
  return request('POST', '/schedule-shifts/update', { ...data, id })
}

export function deleteShift(id) {
  return request('POST', '/schedule-shifts/delete', { id })
}

export function cancelShift(id) {
  return request('POST', '/schedule-shifts/cancel', { id })
}

export function listManagedSchedules(params = {}) {
  const query = []
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') query.push(`${key}=${encodeURIComponent(params[key])}`)
  })
  return request('GET', `/schedules${query.length ? `?${query.join('&')}` : ''}`)
}

export function listScheduleApplicants(scheduleId, params = {}) {
  const query = []
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') query.push(`${key}=${encodeURIComponent(params[key])}`)
  })
  return request('GET', `/schedules/${scheduleId}/applicants${query.length ? `?${query.join('&')}` : ''}`)
}

export function updateManagedSchedule(data) {
  return request('POST', '/schedules/update', data)
}

export function copyManagedSchedule(data) {
  return request('POST', '/schedules/copy', data)
}

export function batchCreateManagedSchedules(data) {
  return request('POST', '/schedules/batch-create', data)
}
