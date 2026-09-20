import request from './request'

export function listActivities(params) {
  return request.post('/activities/list', params)
}

export function createActivity(data) {
  return request.post('/activities/create', data)
}

export function updateActivity(data) {
  return request.post('/activities/update', data)
}

export function toggleActivity(id, status) {
  return request.post('/activities/toggle', { id, status })
}

export function deleteActivity(id) {
  return request.post('/activities/delete', { id })
}

export function getActivityEffectStats(id) {
  return request.post('/activities/effect-stats', { id })
}

export function listPushTasks(params) {
  return request.post('/activities/push-tasks', params)
}

export function createPushTask(data) {
  return request.post('/activities/push/create', data)
}
