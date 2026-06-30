import request from './request'

export function listSchedulesByJobId(jobId) {
  return request.post('/schedules/list-by-job', { jobId })
}

export function detailSchedule(id) {
  return request.post('/schedules/detail', { id })
}

export function cancelSchedule(id) {
  return request.post('/schedules/cancel', { id })
}
