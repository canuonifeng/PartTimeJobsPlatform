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
  return request.post('/jobs/update', { ...data, id })
}

export function deleteJob(id) {
  return request.post('/jobs/delete', { id })
}

export function publishJob(id) {
  return request.post('/jobs/publish', { id })
}

export function closeJob(id) {
  return request.post('/jobs/close', { id })
}

export function reopenJob(id) {
  return request.post('/jobs/reopen', { id })
}

export function getJobRates(jobId) {
  return request.get('/jobs/rates', { params: { jobId } })
}

export function addJobRate(jobId, data) {
  return request.post('/jobs/rates', { ...data, jobId })
}

export function updateJobRate(jobId, rateId, data) {
  return request.post('/jobs/rates/update', { ...data, jobId, rateId })
}

export function removeJobRate(jobId, rateId) {
  return request.post('/jobs/rates/delete', { jobId, rateId })
}

export function getJobSchedules(jobId) {
  return request.get('/jobs/schedules', { params: { jobId } })
}

export function addJobSchedule(jobId, data) {
  return request.post('/jobs/schedules', { ...data, jobId })
}

export function updateJobSchedule(jobId, scheduleId, data) {
  return request.post('/jobs/schedules/update', { ...data, jobId, id: scheduleId })
}

export function removeJobSchedule(jobId, scheduleId) {
  return request.post('/jobs/schedules/delete', { jobId, id: scheduleId })
}
