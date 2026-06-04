import request from './request'

export function getHomeStats() {
  return request({
    url: '/api/home/stats',
    method: 'GET'
  })
}

export function getHomeSchedules() {
  return request({
    url: '/api/home/schedules',
    method: 'GET'
  })
}
