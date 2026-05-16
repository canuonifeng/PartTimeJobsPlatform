import request from './request'

export function listWorkers(params) {
  return request.post('/admin/workers/list', params)
}

export function detailWorker(id) {
  return request.post('/admin/workers/detail', { id })
}

export function updateWorker(data) {
  return request.post('/admin/workers/update', data)
}

export function banWorker(id) {
  return request.post('/admin/workers/ban', { id })
}

export function unbanWorker(id) {
  return request.post('/admin/workers/unban', { id })
}
