<script setup lang="ts">
import type { dynamic } from '@compose/types'
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'
import { YDragInjectionSymbol } from '@/components/drag/YDrag'

const props = withDefaults(defineProps<Props>(), {
  tag: 'div',
  maskTag: 'div',
  hoverMask: true,
  effectAllow: 'copy',
  format: 'text/plain',
})

const emits = defineEmits<Emits>()

const inj = inject(YDragInjectionSymbol)

interface Props extends ModelValueProps<dynamic> {
  tag?: keyof HTMLElementTagNameMap

  maskTag?: keyof HTMLElementTagNameMap
  hoverMask?: boolean
  format?: 'application/json' | 'text/plain'
  effectAllow?: 'none' | 'copy' | 'copyLink' | 'copyMove' | 'link' | 'linkMove' | 'move' | 'all' | 'uninitialized'
}

interface Emits extends ModelValueEmits<dynamic> {
  (e: 'enter' | 'over' | 'leave', ev: DragEvent): void

  (e: 'drop', data: dynamic, ev: DragEvent): void
}

const _modelValue = useVModel(props, 'modelValue', emits, { passive: true })

watch(
  () => inj?.isDrag.value,
  (v) => {
    if (v) {
      _modelValue.value = void 0
    }
  },
)

function dragEnter(e: DragEvent) {
  e.preventDefault()
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = props.effectAllow
  }
  emits('enter', e)
}
function dragLeave(e: DragEvent) {
  emits('leave', e)
}
function dragOver(e: DragEvent) {
  e.preventDefault()
  emits('over', e)
}
function dragDrop(e: DragEvent) {
  e.dataTransfer?.clearData()
  if (inj) {
    inj.isDrag.value = false
    inj.data.value = inj.dragData.value
    _modelValue.value = inj.data.value
    emits('drop', _modelValue.value, e)
  }
}

const wrapper = ref<null | HTMLElement>(null)
const { width, height } = useElementSize(wrapper)
const canHover = computed(() => {
  const b = props.hoverMask
  return !b ? true : inj?.isDrag.value
})
</script>

<template>
<Component :is="props.tag" ref="wrapper" class="relative wh-full transition-all duration-150">
  <Component
    :is="maskTag"
    v-if="canHover"
    class="absolute h-full w-full bg-m transition-all duration-150"
    @dragenter="dragEnter"
    @dragleave="dragLeave"
    @drop="dragDrop"
    @dragover="dragOver"
  >
    <slot name="mask" :isDrag="inj?.isDrag.value ?? false" :width="width" :height="height" />
  </Component>
  <slot name="default" :isDrag="inj?.isDrag.value ?? false" :width="width" :height="height" />
</Component>
</template>
