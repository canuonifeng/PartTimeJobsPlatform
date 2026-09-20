import request from './request'

export function listWorkerMappings(params) {
  return request.get('/worker-mappings', { params })
}

export function createWorkerMapping(data) {
  return request.post('/worker-mappings', data)
}

export function deleteWorkerMapping(id) {
  return request.post('/worker-mappings/delete', { id })
}
