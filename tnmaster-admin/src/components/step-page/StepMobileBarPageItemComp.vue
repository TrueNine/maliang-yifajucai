<script setup lang="ts">
import type { dynamic } from '@compose/types'

interface Props<T = dynamic> {
  value?: T
}
type Emits<T = dynamic> = (e: 'update:value', value: T) => void
const props = withDefaults(defineProps<Props>(), {
  value: 0,
})
const emits = defineEmits<Emits>()

const _value = useVModel(props, 'value', emits, { passive: true })
</script>

<template>
<VWindowItem disabled :value="_value">
  <slot name="top" />
  <slot name="default" />
</VWindowItem>
</template>
