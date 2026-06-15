const listeners = []
let pendingAction = null

export function openLoginSheet(options = {}) {
  pendingAction = typeof options.success === 'function' ? options.success : null
  listeners.forEach((listener) => listener({ visible: true }))
}

export function onLoginSheetChange(listener) {
  listeners.push(listener)
  return () => {
    const index = listeners.indexOf(listener)
    if (index >= 0) listeners.splice(index, 1)
  }
}

export function closeLoginSheet(options = {}) {
  listeners.forEach((listener) => listener({ visible: false, reset: options.reset === true }))
}

export function notifyLoginSuccess() {
  const action = pendingAction
  pendingAction = null
  closeLoginSheet({ reset: true })
  if (action) action()
}
