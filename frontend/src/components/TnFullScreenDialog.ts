import type { InjectionKey } from 'vue'
import { inject } from 'vue'

export interface FullScreenDialogInjection {
  close: () => void
  open: () => void
}

export const FullScreenDialogInjectionKey: InjectionKey<FullScreenDialogInjection> = Symbol('InjectionKey<FullScreenDialogInjection>')

export function useFullScreenDialog() {
  const dialog = inject(FullScreenDialogInjectionKey)
  if (!dialog) {
    throw new Error('useFullScreenDialog() is called without provider. add FullScreenDialog Component to top level scope')
  }
  return dialog
}
