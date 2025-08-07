import type { latenil } from '@compose/types'
import type { Ref } from 'vue'
import { computed, isRef, toRef } from 'vue'

// FIXME 正则不灵活
export function useFormatChinaPhone<F extends string = string>(phone: Ref<latenil<F>> | latenil<F>, sep = ' ') {
  const ref = isRef(phone) ? phone : toRef(phone)

  return computed({
    get: (): F => {
      const len = ref.value?.length ?? 0
      if (len < 3 || len > 11) {
        return ref.value as F
      }
      const min = Math.min(7, len)
      return [ref.value?.slice(0, 3), ref.value?.slice(3, min), ref.value?.slice(min)].filter(Boolean).join(sep) as F
    },
    set: (v: F) => {
      const _v = v.replace(/\s/g, '')
      ref.value = _v as F
    },
  })
}
