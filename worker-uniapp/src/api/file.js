import request from './request'

export function getSts(biz) {
  return request({ url: `/files/sts?biz=${encodeURIComponent(biz)}`, method: 'POST' })
}

export function getSignedUrl(key) {
  return request({ url: '/files/signed-url', method: 'GET', data: { key } })
}
