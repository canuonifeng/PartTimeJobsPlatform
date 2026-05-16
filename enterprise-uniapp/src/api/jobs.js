import { request } from './request'

export function getJobs(companyId, status = '') {
  let url = `/jobs?companyId=${companyId}`
  if (status) url += `&status=${status}`
  return request('GET', url)
}

export function getJob(id) {
  return request('GET', `/jobs?id=${id}`)
}

export function createJob(data) {
  return request('POST', '/jobs', data)
}

export function updateJob(id, data) {
  return request('PUT', `/jobs?id=${id}`, data)
}

export function deleteJob(id) {
  return request('DELETE', `/jobs?id=${id}`)
}

export function publishJob(id) {
  return request('PUT', `/jobs/publish?id=${id}`)
}

export function closeJob(id) {
  return request('PUT', `/jobs/close?id=${id}`)
}

export function reopenJob(id) {
  return request('PUT', `/jobs/reopen?id=${id}`)
}

export function getCategories() {
  return request('GET', '/job-categories')
}

export function getRates(jobId) {
  return request('GET', `/jobs/rates?jobId=${jobId}`)
}

export function createRate(jobId, data) {
  return request('POST', `/jobs/rates?jobId=${jobId}`, data)
}

export function updateRate(jobId, rateId, data) {
  return request('PUT', `/jobs/rates?jobId=${jobId}&rateId=${rateId}`, data)
}

export function deleteRate(jobId, rateId) {
  return request('DELETE', `/jobs/rates?jobId=${jobId}&rateId=${rateId}`)
}

export function getSchedules(jobId) {
  return request('GET', `/jobs/schedules?jobId=${jobId}`)
}

export function createSchedule(jobId, data) {
  return request('POST', `/jobs/schedules?jobId=${jobId}`, data)
}

export function updateSchedule(jobId, scheduleId, data) {
  return request('PUT', `/jobs/schedules?jobId=${jobId}&scheduleId=${scheduleId}`, data)
}

export function deleteSchedule(jobId, scheduleId) {
  return request('DELETE', `/jobs/schedules?jobId=${jobId}&scheduleId=${scheduleId}`)
}
