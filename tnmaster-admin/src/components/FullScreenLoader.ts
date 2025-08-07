import type { bool, timestamp } from '@compose/types'
import type { InjectionKey } from 'vue'
import { inject } from 'vue'

export interface Props {
  show?: bool
  delay?: timestamp
  autoClose?: bool
}

export type Emits = (e: 'update:show', show: bool) => void
export interface FullScreenLoaderInjection {
  load: () => void
  close: () => void
}

export const FullScreenLoaderSymbol: InjectionKey<FullScreenLoaderInjection> = Symbol('VuetifyFullScreenLoaderSymbol')

export function useFullScreenLoader() {
  return inject(FullScreenLoaderSymbol)
}
