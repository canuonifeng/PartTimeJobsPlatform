import { request } from './request'

export function getEnterpriseInfo() {
  return request('GET', '')
}

export function updateCompanyLogo(companyLogo) {
  return request('POST', '/logo', { companyLogo })
}
