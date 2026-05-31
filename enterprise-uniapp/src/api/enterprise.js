import { request } from './request'

export function getEnterpriseInfo() {
  return request('GET', '/enterprise')
}

export function updateCompanyLogo(companyLogo) {
  return request('PUT', '/enterprise/logo', { companyLogo })
}
