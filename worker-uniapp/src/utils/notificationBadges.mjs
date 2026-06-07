export function hasUnreadMessages(messages) {
  return Array.isArray(messages) && messages.some((message) => !message.read)
}

export function hasUnreadCategory(messages, category) {
  return Array.isArray(messages) && messages.some((message) => message.category === category && !message.read)
}
