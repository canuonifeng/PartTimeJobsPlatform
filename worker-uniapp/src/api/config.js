import request from './request'

let cachedRadius = null

export async function getCheckInRadius() {
  if (cachedRadius !== null) return cachedRadius
  try {
    const data = await request({
      url: '/auth/configs?key=check_in_radius_meters',
      method: 'GET'
    })
    cachedRadius = parseInt(data?.value) || 100
  } catch {
    cachedRadius = 100
  }
  return cachedRadius
}

let cachedAnnotationPlatformUrl = null

export async function getAnnotationPlatformUrl() {
  if (cachedAnnotationPlatformUrl !== null) return cachedAnnotationPlatformUrl
  try {
    const data = await request({
      url: '/auth/configs?key=annotation_platform_url',
      method: 'GET'
    })
    cachedAnnotationPlatformUrl = data?.value || ''
  } catch {
    cachedAnnotationPlatformUrl = ''
  }
  return cachedAnnotationPlatformUrl
}