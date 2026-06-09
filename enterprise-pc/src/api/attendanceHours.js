import request from './request'

export function listAttendanceHours(params) {
  return request.get('/attendance/hours', { params })
}

export function updateAttendanceHours(id, data) {
  return request.put('/attendance/hours', data, { params: { id } })
}

export function batchPayAttendanceHours(data) {
  return request.put('/attendance/hours/pay', data)
}

export function batchDeleteAttendanceHours(data) {
  return request.delete('/attendance/hours', { data })
}

export function unsettleAttendanceHours(id) {
  return request.put('/settlement/unsettle', null, { params: { attendanceRecordId: id } })
}
