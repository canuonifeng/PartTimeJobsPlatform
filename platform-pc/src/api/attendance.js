import request from './request'

export function listAttendance(params) {
  return request.post('/attendance/list', params)
}

export function detailAttendance(id) {
  return request.post('/attendance/detail', { id })
}

export function updateAttendanceStatus(id, status, remark) {
  return request.post('/attendance/update-status', { id, status, remark })
}
