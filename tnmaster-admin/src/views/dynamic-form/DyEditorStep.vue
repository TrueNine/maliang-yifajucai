<script setup lang="ts">
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'

type Props = ModelValueProps<number>
interface Emits extends ModelValueEmits<number> {
  (e: 'clear'): void
  (e: 'remove', step: number): void
}
const props = withDefaults(defineProps<Props>(), {
  modelValue: void 0,
})
const emits = defineEmits<Emits>()
const currentStep = useVModel(props, 'modelValue', emits, { passive: true })

const steps = ref<number[]>([])
function addStep() {
  steps.value.push(steps.value.length)
}

function clearAllStep() {
  steps.value = []
  emits('clear')
}
function removeStep(step: number) {
  steps.value = steps.value.filter((s) => s !== step)
  emits('remove', step)
}
</script>

<template>
<div class="mb-4 f-y-c justify-between px-4">
  <VTabs v-model="currentStep">
    <VTab v-for="(step, i) in steps" :key="i" :value="step">
      <div class="min-w-24 w-full f-y-c flex justify-between">
        <span>步骤 {{ step + 1 }}</span>
        <YIco class="i-mdi:close" @click="removeStep(i)" />
      </div>
    </VTab>
  </VTabs>
  <div>
    <VBtn variant="text" @click="addStep">
      <YIco class="i-mdi:add" />
    </VBtn>
    <VBtn class="c-red" variant="text" @click="clearAllStep">
      <YIco class="i-mdi:close" />
    </VBtn>
  </div>
</div>
</template>
