import type { dynamic } from '@compose/types'
import type { Ref } from 'vue'

type ConfirmFn<T = unknown> = (value?: T) => void | Promise<void>

interface UseLazyConfirmOptions<T = unknown> {
  onConfirm?: ConfirmFn<T>
  onOk?: (value?: T) => void | Promise<void>
  onCancel?: (value?: T) => void
  onFinally?: (value?: T) => void
  show?: Ref<boolean | undefined | null>
}

export function confirmation<T = unknown>(options: UseLazyConfirmOptions<T> | ConfirmFn<T> = {}, initialValue?: T) {
  const {
    onConfirm,
    onOk,
    onCancel,
    onFinally,
    show,
  } = typeof options === 'object' ? options : { onConfirm: options }
  let resolveHandler: ((value?: T) => void) | undefined
  let rejectHandler: ((reason?: dynamic) => void) | undefined
  let currentValue = initialValue

  const handler = new Promise<T | undefined>((resolve, reject) => {
    resolveHandler = resolve
    rejectHandler = reject
  })
  const _setDialog = (value: boolean) => {
    if (show === void 0) {
      return
    }
    show.value = value
  }
  const confirm = async (newValue?: T) => {
    _setDialog(true)
    currentValue = newValue
    await onConfirm?.(currentValue)
  }

  const ok = async () => {
    try {
      await onOk?.(currentValue)
      resolveHandler?.(currentValue)
    } finally {
      onFinally?.(currentValue)
      _setDialog(false)
    }
  }

  const cancel = () => {
    try {
      onCancel?.(currentValue)
      rejectHandler?.(currentValue)
    } finally {
      onFinally?.(currentValue)
      _setDialog(false)
    }
  }

  return {
    confirm,
    ok,
    cancel,
    handler,
  }
}

export * from './IsMobile'
