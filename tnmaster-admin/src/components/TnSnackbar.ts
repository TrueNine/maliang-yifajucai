import type { InjectionKey } from 'vue'
import { inject } from 'vue'

export interface SnackbarOptions {
  type?: 'success' | 'warning' | 'info' | 'error'
  message?: string
  errorMessage?: string
  loadingMessage?: string
  position?: 'top' | 'bottom' | 'center'
  onError?: (e: Error) => void
  onFinally?: () => void
}
export type SnackbarSuccessOptions = Omit<SnackbarOptions, 'type' | 'message' | 'errorMessage' | 'loadingMessage'>
export type SnackbarErrorOptions = Omit<SnackbarOptions, 'type' | 'message' | 'errorMessage' | 'loadingMessage'>

export type SnackbarMessageType<T = unknown> = string | T | Promise<T> | (() => T)

export interface SnackbarInjection {
  success: (msg: string, options?: SnackbarSuccessOptions) => void
  error: (msg: string, options?: SnackbarErrorOptions) => void
  message: <T = unknown>(message: SnackbarMessageType<T>, options?: SnackbarOptions) => Promise<T | undefined>
  loading: <T = unknown>(message: SnackbarMessageType<T>, options?: SnackbarOptions) => Promise<T | undefined>
}

export const SnackbarInjectionSymbol: InjectionKey<SnackbarInjection> = Symbol('SnackbarInjectionSymbol')

export function useSnackbar() {
  const snackbar = inject(SnackbarInjectionSymbol)
  if (!snackbar) {
    throw new Error('useSnackbar() is called without provider. add Snackbar Component to top level scope')
  }
  return snackbar
}
