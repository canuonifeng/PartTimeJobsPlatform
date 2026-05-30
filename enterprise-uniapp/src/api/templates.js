import { request } from './request'

export function listTemplates() {
  return request('POST', '/templates/list')
}

export function createTemplate(data) {
  return request('POST', '/templates/create', data)
}

export function updateTemplate(data) {
  return request('POST', '/templates/update', data)
}

export function deleteTemplate(id) {
  return request('POST', '/templates/delete', { id })
}
