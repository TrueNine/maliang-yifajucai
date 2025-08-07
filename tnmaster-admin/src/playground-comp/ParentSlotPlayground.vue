<script setup lang="tsx">
import type { late, nil } from '@compose/types'
import type { ComponentInternalInstance } from 'vue'

const slots = defineSlots<{
  default: (a: late<string>) => VNode[]
}>()
const initSlotData = ref<string>()
const comp1Ref = shallowRef<nil<ComponentInternalInstance>>()
function SlotRendererComponent() {
  if (slots && slots.default) {
    const vNodes = slots.default(initSlotData.value)
    void nextTick(() => {
      if (vNodes) {
        const e = vNodes.filter((r) => r && r.type && r.component)
        const firstNode = e[0]
        if (firstNode) {
          comp1Ref.value = firstNode.component
        }
      }
    })
    return vNodes
  }
}
const counter = ref(0)
function onClick() {
  initSlotData.value = `changed${counter.value++}`
}
</script>

<template>
<div>
  <VBtn color="primary" @click="onClick">
    调用插槽方法
  </VBtn>
  <Component :is="SlotRendererComponent" />
</div>
</template>
