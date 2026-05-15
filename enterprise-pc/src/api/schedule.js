import request from './request'

export function listTemplates(params) {
  return request.get('/schedule-templates', { params })
}

export function getTemplate(id) {
  return request.get(`/schedule-templates/${id}`)
}

export function createTemplate(data) {
  return request.post('/schedule-templates', data)
}

export function updateTemplate(id, data) {
  return request.put(`/schedule-templates/${id}`, data)
}

export function deleteTemplate(id) {
  return request.delete(`/schedule-templates/${id}`)
}

export function listShifts(params) {
  return request.get('/schedule-shifts', { params })
}

export function createShift(data) {
  return request.post('/schedule-shifts', data)
}

export function updateShift(id, data) {
  return request.put(`/schedule-shifts/${id}`, data)
}

export function deleteShift(id) {
  return request.delete(`/schedule-shifts/${id}`)
}
