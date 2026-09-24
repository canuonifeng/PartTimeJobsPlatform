import request from './request'

export function getSts(biz) {
  return request({ url: '/files/sts', method: 'POST', data: { biz } })
}

export function getSignedUrl(key) {
  return request({ url: '/files/signed-url', method: 'GET', data: { key } })
}
