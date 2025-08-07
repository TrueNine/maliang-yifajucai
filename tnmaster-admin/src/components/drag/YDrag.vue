<script setup lang="ts">
import type { dynamic } from '@compose/types'
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'
import { YDragInjectionSymbol } from './YDrag'

const props = withDefaults(defineProps<ModelValueProps<dynamic>>(), {
  modelValue: void 0,
})
const emits = defineEmits<ModelValueEmits<dynamic>>()
const _mv = useVModel(props, 'modelValue', emits, { passive: true })
const data = ref()
const dragData = ref()
const isDrag = ref<boolean>()
syncRef(data, _mv, { direction: 'ltr' })
watch(
  () => dragData.value,
  (v) => {
    if (v) {
      data.value = void 0
    }
  },
)
const format = ref<string>()
provide(YDragInjectionSymbol, { data, format, dragData, isDrag })
</script>

<template>
<slot name="default" />
</template>
