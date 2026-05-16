import request from './request'

export function listWorkers(params) {
  return request.post('/company-workers/list', params)
}

export function detailWorker(id) {
  return request.post('/company-workers/detail', { id })
}

export function blacklistWorker(id) {
  return request.post('/company-workers/blacklist', { id })
}

export function unblacklistWorker(id) {
  return request.post('/company-workers/unblacklist', { id })
}
