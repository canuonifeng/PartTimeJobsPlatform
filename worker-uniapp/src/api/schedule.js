import request from './request'

export function getMyShifts(params) {
  return request({
    url: '/api/schedule-shifts/my',
    method: 'GET',
    data: params
  })
}
