import request from '@/api/request'

export function sendSmsCode(phone) {
  return request({
    url: '/auth/send-code',
    method: 'POST',
    data: { phone }
  })
}

export function phoneLogin(phone, code) {
  return request({
    url: '/auth/phone-login',
    method: 'POST',
    data: { phone, code }
  })
}

export function wechatPhoneLogin(code, encryptedData, iv) {
  return request({
    url: '/auth/wechat-phone-login',
    method: 'POST',
    data: { code, encryptedData, iv }
  })
}

export function getProfile() {
  return request({
    url: '/auth/profile',
    method: 'GET'
  })
}
