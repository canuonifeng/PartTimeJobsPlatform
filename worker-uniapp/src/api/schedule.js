import request from './request'

export function getMyShifts() {
  return request({
    url: '/api/worker/schedule-shifts/my',
    method: 'GET'
  })
}
