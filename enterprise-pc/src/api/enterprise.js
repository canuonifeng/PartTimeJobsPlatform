import request from './request'

export function getEnterpriseInfo() {
  return request.get('/enterprise')
}

export function updateCompanyLogo(companyLogo) {
  return request.post('/enterprise/logo', { companyLogo })
}
