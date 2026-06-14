import request from './request'

export function getHomeStats() {
  return request({
    url: '/home/stats',
    method: 'GET'
  })
}

export function getHomeSchedules() {
  return request({
    url: '/home/schedules',
    method: 'GET'
  })
}
