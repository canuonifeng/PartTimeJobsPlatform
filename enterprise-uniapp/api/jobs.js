import { request } from './request'

export function getJobs(companyId, status = '') {
  let url = `/jobs?companyId=${companyId}`
  if (status) url += `&status=${status}`
  return request('GET', url)
}

export function getJob(id) {
  return request('GET', `/jobs/${id}`)
}

export function createJob(data) {
  return request('POST', '/jobs', data)
}

export function updateJob(id, data) {
  return request('PUT', `/jobs/${id}`, data)
}

export function deleteJob(id) {
  return request('DELETE', `/jobs/${id}`)
}

export function publishJob(id) {
  return request('PUT', `/jobs/${id}/publish`)
}

export function closeJob(id) {
  return request('PUT', `/jobs/${id}/close`)
}

export function reopenJob(id) {
  return request('PUT', `/jobs/${id}/reopen`)
}

export function getCategories() {
  return request('GET', '/job-categories')
}

export function getRates(jobId) {
  return request('GET', `/jobs/${jobId}/rates`)
}

export function createRate(jobId, data) {
  return request('POST', `/jobs/${jobId}/rates`, data)
}

export function getSchedules(jobId) {
  return request('GET', `/jobs/${jobId}/schedules`)
}

export function createSchedule(jobId, data) {
  return request('POST', `/jobs/${jobId}/schedules`, data)
}
