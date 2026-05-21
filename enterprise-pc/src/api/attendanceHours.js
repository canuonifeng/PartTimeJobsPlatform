import request from './request'

export function listAttendanceHours(params) {
  return request.get('/attendance/hours', { params })
}

export function updateAttendanceHours(id, data) {
  return request.put(`/attendance/hours/${id}`, data)
}

export function batchPayAttendanceHours(data) {
  return request.put('/attendance/hours/pay', data)
}

export function batchDeleteAttendanceHours(data) {
  return request.delete('/attendance/hours', { data })
}
