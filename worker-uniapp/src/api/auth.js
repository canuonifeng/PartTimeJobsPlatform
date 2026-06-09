import request from '@/api/request'

export function sendSmsCode(phone) {
  return request({
    url: '/api/auth/send-code',
    method: 'POST',
    data: { phone }
  })
}

export function phoneLogin(phone, code) {
  return request({
    url: '/api/auth/phone-login',
    method: 'POST',
    data: { phone, code }
  })
}

export function wechatPhoneLogin(code, encryptedData, iv) {
  return request({
    url: '/api/auth/wechat-phone-login',
    method: 'POST',
    data: { code, encryptedData, iv }
  })
}

export function getProfile() {
  return request({
    url: '/api/auth/profile',
    method: 'GET'
  })
}
