import request from './request'

export function getJobDetail(id) {
  return request.post('/jobs/detail', { id })
}

export function listApplicationsByJob(jobId) {
  return request.post('/applications/list-by-job', { jobId })
}

export function listSchedulesByJob(jobId) {
  return request.post('/schedules/list-by-job', { jobId })
}

export function listAttendanceByJob(jobId) {
  return request.post('/attendance/list-by-job', { jobId })
}

export function listSettlementsByCompany(companyId) {
  return request.post('/settlements/list', { companyId })
}
