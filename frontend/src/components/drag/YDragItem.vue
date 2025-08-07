<script setup lang="ts">
import type { dynamic } from '@compose/types'
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'
import { YDragInjectionSymbol } from '@/components/drag/YDrag'

const props = withDefaults(defineProps<Props>(), {
  modelValue: void 0,
  value: void 0,
  tag: 'div',
  disabled: false,
  format: 'text/plain',
})

const emits = defineEmits<Emits>()

const inj = inject(YDragInjectionSymbol)

interface Props extends ModelValueProps<dynamic> {
  tag?: keyof HTMLElementTagNameMap
  disabled?: boolean
  format?: 'application/json' | 'text/plain'
}

interface Emits extends ModelValueEmits<dynamic> {
  (e: 'update:x' | 'update:y', location: number): void

  (e: 'over' | 'start' | 'end', ev: DragEvent): void
}

const _modelValue = useVModel(props, 'modelValue', emits, { passive: true })

const formatValue = ref()
watch(
  () => _modelValue.value,
  (v) => {
    if (props.format === 'text/plain') {
      formatValue.value = JSON.stringify(v)
    } else {
      formatValue.value = v
    }
  },
  { deep: true, immediate: true },
)

function dragStart(e: DragEvent) {
  emits('start', e)
  if (inj) {
    inj.dragData.value = _modelValue.value
    inj.isDrag.value = true
  }
}
function dragEnd(e: DragEvent) {
  emits('end', e)
  e.dataTransfer?.clearData(props.format)
  if (inj) {
    inj.dragData.value = void 0
    inj.isDrag.value = false
  }
}
function dragOver(e: DragEvent) {
  e.preventDefault()
  emits('over', e)
}
</script>

<template>
<Component
  :is="props.tag"
  :class="{ 'cursor-pointer': !disabled, 'cursor-not-allowed': disabled }"
  :draggable="!disabled"
  @dragover="dragOver"
  @dragstart="dragStart"
  @dragend="dragEnd"
>
  <slot name="default" :disabled="disabled" />
</Component>
</template>
