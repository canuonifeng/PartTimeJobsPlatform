import request from './request'

export function listJobs(params) {
  return request.get('/jobs', { params })
}

export function getJob(id) {
  return request.get('/jobs', { params: { id } })
}

export function getJobTags() {
  return request.get('/job-tags')
}

export function getJobShareLink(id) {
  return request.get('/jobs/share-link', { params: { id } })
}

export function createJob(data) {
  return request.post('/jobs', data)
}

export function updateJob(id, data) {
  return request.put('/jobs', data, { params: { id } })
}

export function deleteJob(id) {
  return request.delete('/jobs', { params: { id } })
}

export function publishJob(id) {
  return request.put('/jobs/publish', null, { params: { id } })
}

export function closeJob(id) {
  return request.put('/jobs/close', null, { params: { id } })
}

export function reopenJob(id) {
  return request.put('/jobs/reopen', null, { params: { id } })
}

export function getJobRates(jobId) {
  return request.get('/jobs/rates', { params: { jobId } })
}

export function addJobRate(jobId, data) {
  return request.post('/jobs/rates', data, { params: { jobId } })
}

export function updateJobRate(jobId, rateId, data) {
  return request.put('/jobs/rates', data, { params: { jobId, rateId } })
}

export function removeJobRate(jobId, rateId) {
  return request.delete('/jobs/rates', { params: { jobId, rateId } })
}

export function getJobSchedules(jobId) {
  return request.get('/jobs/schedules', { params: { jobId } })
}

export function addJobSchedule(jobId, data) {
  return request.post('/jobs/schedules', data, { params: { jobId } })
}

export function updateJobSchedule(jobId, scheduleId, data) {
  return request.put('/jobs/schedules', data, { params: { jobId, scheduleId } })
}

export function removeJobSchedule(jobId, scheduleId) {
  return request.delete('/jobs/schedules', { params: { jobId, scheduleId } })
}
