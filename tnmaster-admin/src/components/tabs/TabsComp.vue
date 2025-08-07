<script setup lang="ts">
import type { bool, dynamic, i32 } from '@compose/types'

import { findSlotNodesByName } from '@compose/external/vue'

export interface Props {
  value?: i32
  color?: string
  fixedTabs?: bool
  showArrows?: bool
}

export type Emits = (e: 'update:value', v?: i32) => void

type ItemSlot = () => dynamic
export type Slots = {
  'w-1': ItemSlot
  'w-2': ItemSlot
  'w-3': ItemSlot
  'w-4': ItemSlot
  'w-5': ItemSlot
  'w-6': ItemSlot
  'w-7': ItemSlot
  'w-8': ItemSlot
  'w-9': ItemSlot
  'w-10': ItemSlot
  'w-11': ItemSlot
  'w-12': ItemSlot
  'w-13': ItemSlot
  'w-14': ItemSlot
  'w-15': ItemSlot
  'w-16': ItemSlot
  'w-17': ItemSlot
  'w-18': ItemSlot
  'w-19': ItemSlot
  'w-20': ItemSlot
} & {
  default?: () => dynamic
}

const props = withDefaults(defineProps<Props>(), {
  value: 1,
  color: 'secondary',
  fixedTabs: true,
  showArrows: true,
})
const emits = defineEmits<Emits>()
defineSlots<Slots>()
const { value: _value } = useVModels(props, emits, { passive: true })
const slots = useSlots()
const windows = findSlotNodesByName('VTab', slots.default?.())
windows.forEach((e, i) => {
  const c = calcStep(i)
  if (!e.props) {
    e.props = { value: c }
  } else {
    e.props.value = c
  }
})

function calcStep(s: number): 1 | 2 {
  return (s + 1) as 1
}
</script>

<template>
<VTabs v-model="_value" :bgColor="props.color" :fixedTabs="props.fixedTabs">
  <slot v-if="false" name="default" />
  <template v-for="(it, _) in windows" :key="_">
    <component :is="it" />
  </template>
</VTabs>
<VWindow v-model="_value">
  <VWindowItem v-for="(_, idx) in windows" :key="idx" :value="calcStep(idx)">
    <slot :name="`w-${calcStep(idx)}`" />
  </VWindowItem>
</VWindow>
</template>
