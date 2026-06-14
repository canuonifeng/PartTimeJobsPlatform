import request from './request'

export function listShifts(params) {
  return request.get('/schedule-shifts', { params })
}

export function createShift(data) {
  return request.post('/schedule-shifts', data)
}

export function updateShift(id, data) {
  return request.post('/schedule-shifts/update', { ...data, id })
}

export function deleteShift(id) {
  return request.post('/schedule-shifts/delete', { id })
}

export function cancelShift(id) {
  return request.post('/schedule-shifts/cancel', { id })
}

export function getAttendanceReport(params) {
  return request.get('/attendance/report', { params })
}

export function listCorrections(params) {
  return request.get('/schedules/corrections', { params })
}

export function approveCorrection(id) {
  return request.post('/schedules/corrections/approve', { id })
}

export function rejectCorrection(id, data) {
  return request.post('/schedules/corrections/reject', { ...data, id })
}
