import request from './request'

export function listAttendanceHours(params) {
  return request.get('/attendance/hours', { params })
}

export function updateAttendanceHours(id, data) {
  return request.post('/attendance/hours/update', { ...data, id })
}

export function batchPayAttendanceHours(data) {
  return request.post('/attendance/hours/pay', { ids: data })
}

export function batchDeleteAttendanceHours(data) {
  return request.post('/attendance/hours/delete', { ids: data })
}

export function unsettleAttendanceHours(id) {
  return request.post('/settlement/unsettle', { attendanceRecordId: id })
}
