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
  return request('POST', '/jobs/update', { ...data, id })
}

export function deleteJob(id) {
  return request('POST', '/jobs/delete', { id })
}

export function getJobShareCode(id) {
  return request('GET', `/jobs/share-code?id=${id}`)
}

export function getJobShareLink(id) {
  return request('GET', `/jobs/share-link?id=${id}`)
}

export function getApplications(jobId, params = {}) {
  const query = []
  if (jobId != null) query.push(`jobId=${jobId}`)
  if (params.status) query.push(`status=${encodeURIComponent(params.status)}`)
  if (params.page != null) query.push(`page=${params.page}`)
  if (params.pageSize != null) query.push(`pageSize=${params.pageSize}`)
  return request('GET', `/applications${query.length ? `?${query.join('&')}` : ''}`)
}

export function acceptApplication(applicationId) {
  return request('POST', '/applications/accept', { applicationId })
}

export function rejectApplication(applicationId) {
  return request('POST', '/applications/reject', { applicationId })
}

export function publishJob(id) {
  return request('POST', '/jobs/publish', { id })
}

export function closeJob(id) {
  return request('POST', '/jobs/close', { id })
}

export function reopenJob(id) {
  return request('POST', '/jobs/reopen', { id })
}

export function getCategories() {
  return request('GET', '/job-categories')
}

export function getJobTags() {
  return request('GET', '/job-tags')
}

export function getRates(jobId) {
  return request('GET', `/jobs/rates?jobId=${jobId}`)
}

export function createRate(jobId, data) {
  return request('POST', '/jobs/rates', { ...data, jobId })
}

export function updateRate(jobId, rateId, data) {
  return request('POST', '/jobs/rates/update', { ...data, jobId, rateId })
}

export function deleteRate(jobId, rateId) {
  return request('POST', '/jobs/rates/delete', { jobId, rateId })
}

export function getSchedules(jobId) {
  return request('GET', `/jobs/schedules?jobId=${jobId}`)
}

export function createSchedule(jobId, data) {
  return request('POST', '/jobs/schedules', { ...data, jobId })
}

export function updateSchedule(jobId, scheduleId, data) {
  return request('POST', '/jobs/schedules/update', { ...data, jobId, id: scheduleId })
}

export function deleteSchedule(jobId, scheduleId) {
  return request('POST', '/jobs/schedules/delete', { jobId, id: scheduleId })
}
