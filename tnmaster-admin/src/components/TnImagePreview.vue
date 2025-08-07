<script setup lang="ts">
import type { dynamic } from '@compose/types'
import type {} from 'vuetify/components/VImg'

interface Props {
  modelValue?: boolean
  width?: string
  height?: string
  zIndex?: number
  previewClass?: string | (string | undefined)[] | Record<string, unknown>
}
interface Slots {
  default?: (props: dynamic) => VNode[]
  activator?: (props: dynamic) => VNode[]
}

const props = withDefaults(defineProps<Props>(), {
  width: 'auto',
  height: 'auto',
  modelValue: false,
  previewClass: void 0,
  zIndex: 1,
})
const emit = defineEmits<Emits>()

const slots = defineSlots<Slots>()

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const _modelValue = useVModel(props, 'modelValue', emit, { passive: true })
</script>

<template>
<VDialog
  v-model="_modelValue"
  :zIndex="props.zIndex"
  :height="props.height"
  :width="props.width"
  maxHeight="90%"
  maxWidth="90%"
>
  <template v-if="!!slots.activator" #activator="p">
    <slot name="activator" :props="p.props" />
  </template>
  <template v-if="!!slots.default">
    <slot name="default" />
  </template>
  <template v-else>
    <slot name="activator" />
  </template>
</VDialog>
</template>
