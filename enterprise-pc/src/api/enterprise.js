import request from './request'

export function getEnterpriseInfo() {
  return request.get('')
}

export function updateCompanyLogo(companyLogo) {
  return request.post('/logo', { companyLogo })
}
