import type { Emitter } from 'mitt'
import mitt from 'mitt'

interface Events {
  error: Error
  notification: {
    type: 'success' | 'error' | 'info' | 'warning'
    message: string
  }
  [key: string]: unknown
  [key: symbol]: unknown
  // 在这里添加更多事件类型
}

const emitter: Emitter<Events> = mitt<Events>()

export const eventBus = {
  // 发送错误
  emitError: (error: Error) => emitter.emit('error', error),
  // 监听错误
  onError: (handler: (error: Error) => void) => emitter.on('error', handler),
  // 移除错误监听
  offError: (handler: (error: Error) => void) => emitter.off('error', handler),

  // 发送通知
  emitNotification: (notification: Events['notification']) =>
    emitter.emit('notification', notification),
  // 监听通知
  onNotification: (handler: (notification: Events['notification']) => void) =>
    emitter.on('notification', handler),
  // 移除通知监听
  offNotification: (handler: (notification: Events['notification']) => void) =>
    emitter.off('notification', handler),

  // 清除所有监听器
  clear: () => emitter.all.clear(),
}

export type EventBus = typeof eventBus
