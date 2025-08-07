<script setup lang="ts">
import type { timestamp } from '@compose/types'

import { dateMillis, formatDate } from '@compose/external/dayjs'

export interface Props {
  label?: string
  placeholder?: string
  start?: timestamp
  end?: timestamp
  beforeNow?: boolean
  max?: timestamp
  min?: timestamp
}

export type Emits = (e: 'update:start' | 'update:end', range: timestamp) => void

const props = withDefaults(defineProps<Props>(), {
  value: void 0,
  label: void 0,
  placeholder: void 0,
  start: void 0,
  end: void 0,
  beforeNow: true,
  max: void 0,
  min: void 0,
})
const emits = defineEmits<Emits>()

const { start: __start, end: __end } = useVModels(props, emits, {
  passive: true,
})

const dataRangeComputed = computed({
  get: () => {
    return [__start.value ? formatDate(__start.value) : void 0, __end.value ? formatDate(__end.value) : void 0].filter((e): e is string => Boolean(e))
  },
  set: (v) => {
    const [f, l] = v
    if (f) {
      __start.value = dateMillis(f)
    }
    if (l) {
      __end.value = dateMillis(l)
    }
  },
})
</script>

<template>
<VarDatePicker v-model="dataRangeComputed" range />
</template>
