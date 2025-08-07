import type { late } from '@compose/types'
import type { InjectionKey, Ref } from 'vue'

interface YDragInjection {
  data: Ref
  dragData: Ref
  format: Ref<late<string>>
  isDrag: Ref<late<boolean>>
}

export const YDragInjectionSymbol: InjectionKey<YDragInjection> = Symbol('YDragInjectionSymbol')
