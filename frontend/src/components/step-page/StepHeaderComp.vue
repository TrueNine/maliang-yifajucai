<script setup lang="ts">
import type { i32 } from '@compose/types'

export interface Props {
  modelValue?: i32
  title?: string
  subtitle?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: void 0,
  title: void 0,
  subtitle: void 0,
})
</script>

<template>
<VStepperItem color="success" :title="props.title" :subtitle="props.subtitle" :value="props.modelValue">
  <template #title>
    <header class="text-4">
      {{ title }}
    </header>
  </template>

  <template #subtitle>
    <div class="text-ell pt-1 text-3">
      {{ subtitle }}
    </div>
  </template>
  <slot name="default" />
</VStepperItem>
</template>
