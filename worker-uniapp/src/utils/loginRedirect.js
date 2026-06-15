const DEFAULT_PAGE = '/pages/jobs/jobList'

const LOGIN_PAGES = new Set([
  '/pages/login/login',
  '/pages/login/phoneLogin'
])

const REGISTERED_PAGES = new Set([
  '/pages/index/index',
  '/pages/jobs/jobList',
  '/pages/jobs/jobDetail',
  '/pages/jobs/applyConfirm',
  '/pages/jobs/applySuccess',
  '/pages/message/message',
  '/pages/profile/profile',
  '/pages/profile/edit',
  '/pages/profile/changePhone',
  '/pages/settings/settings',
  '/pages/auth/realName',
  '/pages/bank/bankCard',
  '/pages/schedule/schedule',
  '/pages/signup/signup',
  '/pages/attendance/clockIn',
  '/pages/earnings/earnings',
  '/pages/earnings/withdraw',
  '/pages/referral/referral',
  '/pages/referral/referralRecords'
])

const TAB_PAGES = new Set([
  '/pages/index/index',
  '/pages/jobs/jobList',
  '/pages/message/message',
  '/pages/profile/profile'
])

function splitUrl(url) {
  const [path] = String(url || '').split('?')
  return path.startsWith('/') ? path : `/${path}`
}

export function normalizeLoginRedirect(value) {
  if (!value) return ''
  let redirect = String(value)
  try {
    redirect = decodeURIComponent(redirect)
  } catch {}
  const path = splitUrl(redirect)
  if (LOGIN_PAGES.has(path) || !REGISTERED_PAGES.has(path)) return ''
  return redirect.startsWith('/') ? redirect : `/${redirect}`
}

export function appendRedirect(url, redirect) {
  const normalized = normalizeLoginRedirect(redirect)
  if (!normalized) return url
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}redirect=${encodeURIComponent(normalized)}`
}

export function goAfterLogin(redirect) {
  const normalized = normalizeLoginRedirect(redirect)
  if (!normalized) {
    const pages = getCurrentPages()
    for (let index = pages.length - 2; index >= 0; index--) {
      const route = pages[index]?.route ? `/${pages[index].route}` : ''
      if (route && !LOGIN_PAGES.has(route)) {
        uni.navigateBack({ delta: pages.length - 1 - index })
        return
      }
    }
    uni.switchTab({ url: DEFAULT_PAGE })
    return
  }

  const path = splitUrl(normalized)
  if (TAB_PAGES.has(path)) {
    uni.switchTab({ url: path })
    return
  }

  uni.redirectTo({
    url: normalized,
    fail: () => uni.switchTab({ url: DEFAULT_PAGE })
  })
}

export function getCurrentPageRedirect() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const route = currentPage?.route ? `/${currentPage.route}` : ''
  if (!route || LOGIN_PAGES.has(route)) return DEFAULT_PAGE

  const options = currentPage?.options || {}
  const query = Object.entries(options)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return `${route}${query ? `?${query}` : ''}`
}

export function isLoginPage(route) {
  return LOGIN_PAGES.has(route)
}
