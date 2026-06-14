import request from './request'

export function listWorkers(params) {
  return request.post('/workers/list', params)
}

export function detailWorker(id) {
  return request.post('/workers/detail', { id })
}

export function updateWorker(data) {
  return request.post('/workers/update', data)
}

export function banWorker(id) {
  return request.post('/workers/ban', { id })
}

export function unbanWorker(id) {
  return request.post('/workers/unban', { id })
}
