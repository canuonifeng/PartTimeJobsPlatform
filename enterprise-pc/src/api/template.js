import request from './request'

export function listTemplates() {
  return request.post('/templates/list')
}

export function createTemplate(data) {
  return request.post('/templates/create', data)
}

export function updateTemplate(data) {
  return request.post('/templates/update', data)
}

export function deleteTemplate(id) {
  return request.post('/templates/delete', { id })
}
