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

export function listManagedSchedules(params) {
  return request.get('/schedules', { params })
}

export function listScheduleApplicants(scheduleId, params) {
  return request.get(`/schedules/${scheduleId}/applicants`, { params })
}

export function exportScheduleApplicants(scheduleId, params) {
  return request.get(`/schedules/${scheduleId}/export`, { params })
}

export function updateManagedSchedule(data) {
  return request.post('/schedules/update', data)
}

export function copyManagedSchedule(data) {
  return request.post('/schedules/copy', data)
}

export function batchCreateManagedSchedules(data) {
  return request.post('/schedules/batch-create', data)
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
