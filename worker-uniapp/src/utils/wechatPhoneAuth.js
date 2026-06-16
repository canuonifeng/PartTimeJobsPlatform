export function hasWechatPhoneAuth(event) {
  const detail = event?.detail || {}
  return !!detail.code || (!!detail.encryptedData && !!detail.iv)
}

export function getWechatPhoneAuthFailureMessage(event) {
  const errMsg = event?.detail?.errMsg || ''
  if (errMsg.includes('deny') || errMsg.includes('cancel')) {
    return '已取消手机号授权'
  }
  if (errMsg.includes('privacy')) {
    return '请先完成隐私授权'
  }
  if (errMsg) {
    return `手机号授权失败：${errMsg}`
  }
  return '未获取到手机号授权，请确认小程序已开通手机号能力'
}
