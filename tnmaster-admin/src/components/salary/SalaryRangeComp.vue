<script setup lang="ts">
import type { decimal, late } from '@compose/types'

export interface Props {
  unit?: string
  sep?: string
  freeText?: string
  min?: decimal
  max?: decimal
}

const props = withDefaults(defineProps<Props>(), {
  unit: void 0,
  min: void 0,
  max: void 0,
  sep: '-',
  freeText: '面议',
})

const free = computed(() => {
  return !props.max && !props.min
})

function getKNum(num: decimal) {
  if (!num) {
    return 0
  }
  if (num <= 0) {
    return 0
  }
  if (num < 1000) {
    return Math.floor(num)
  }
  return Math.floor(num * 100) / 100000
}

function getK(num?: decimal): late<string> {
  if (!num) {
    return void 0
  }
  if (num < 1000) {
    return getKNum(num).toLocaleString()
  }
  return `${getKNum(num).toLocaleString()}K`
}
</script>

<template>
<span c-red>
  <strong v-if="!free">{{ unit }}</strong>
  <strong v-else>{{ props.freeText }}</strong>
  <strong v-if="props.min">{{ props.max ? getKNum(props.min) : getK(props.min) }}</strong>
  <strong v-if="props.max">{{ props.sep }}</strong>
  <strong v-if="props.max">{{ getK(props.max) }}</strong>
</span>
</template>
