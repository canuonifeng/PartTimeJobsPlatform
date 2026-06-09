import request from './request'

let cachedRadius = null

export async function getCheckInRadius() {
  if (cachedRadius !== null) return cachedRadius
  try {
    const data = await request({
      url: '/api/auth/configs?key=check_in_radius_meters',
      method: 'GET'
    })
    cachedRadius = parseInt(data?.value) || 100
  } catch {
    cachedRadius = 100
  }
  return cachedRadius
}