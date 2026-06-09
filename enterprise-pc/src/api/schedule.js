import request from './request'

export function listShifts(params) {
  return request.get('/schedule-shifts', { params })
}

export function createShift(data) {
  return request.post('/schedule-shifts', data)
}

export function updateShift(id, data) {
  return request.put('/schedule-shifts', data, { params: { id } })
}

export function deleteShift(id) {
  return request.delete('/schedule-shifts', { params: { id } })
}

export function cancelShift(id) {
  return request.put('/schedule-shifts/cancel', { id })
}

export function getAttendanceReport(params) {
  return request.get('/attendance/report', { params })
}

export function listCorrections(params) {
  return request.get('/schedules/corrections', { params })
}

export function approveCorrection(id) {
  return request.put('/schedules/corrections/approve', null, { params: { id } })
}

export function rejectCorrection(id, data) {
  return request.put('/schedules/corrections/reject', data, { params: { id } })
}
