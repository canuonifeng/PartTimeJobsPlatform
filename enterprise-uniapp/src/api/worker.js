import { request } from './request'

export function listWorkers(params = {}) {
  return request('POST', '/company-workers/list', params)
}

export function blacklistWorker(id) {
  return request('POST', '/company-workers/blacklist', { id })
}

export function unblacklistWorker(id) {
  return request('POST', '/company-workers/unblacklist', { id })
}
