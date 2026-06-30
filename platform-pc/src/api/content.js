import request from './request'

export function getBanners() {
  return request.post('/content/banners')
}

export function createBanner(data) {
  return request.post('/content/banners/create', data)
}

export function updateBanner(data) {
  return request.post('/content/banners/update', data)
}

export function deleteBanner(id) {
  return request.post('/content/banners/delete', { id })
}

export function getHotRecommendations() {
  return request.post('/content/hot-recommendations')
}

export function addHotRecommendation(data) {
  return request.post('/content/hot-recommendations/add', data)
}

export function removeHotRecommendation(id) {
  return request.post('/content/hot-recommendations/remove', { id })
}

export function getServiceFeeRates() {
  return request.post('/content/service-fee-rates')
}

export function updateServiceFeeRate(data) {
  return request.post('/content/service-fee-rates/update', data)
}
