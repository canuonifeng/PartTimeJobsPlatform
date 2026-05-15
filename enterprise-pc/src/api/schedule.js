import request from './request'

export function listTemplates(params) {
  return request.get('/schedule-templates', { params })
}

export function getTemplate(id) {
  return request.get('/schedule-templates', { params: { id } })
}

export function createTemplate(data) {
  return request.post('/schedule-templates', data)
}

export function updateTemplate(id, data) {
  return request.put('/schedule-templates', data, { params: { id } })
}

export function deleteTemplate(id) {
  return request.delete('/schedule-templates', { params: { id } })
}

export function listShifts(params) {
  return request.get('/schedule-shifts', { params })
}

export function createShift(data) {
  return request.post('/schedule-shifts', data)
}

export function deleteShift(id) {
  return request.delete('/schedule-shifts', { params: { id } })
}

export function getAttendanceReport(params) {
  return request.get('/attendance/report', { params })
}
