<script setup lang="ts">
import type { Emits, Props } from './SalaryRangeSelect'

import { salaryRange } from './SalaryRangeSelect'

const props = withDefaults(defineProps<Props>(), {
  fixed: false,
})
const emits = defineEmits<Emits>()
const _fixed = computed({
  get: () => props.fixed,
  set: (v) => {
    emits('update:fixed', v)
  },
})
const miniSalary = computed({
  get: () => props.minSalary,
  set: (v) => {
    if (!v) {
      return
    }
    emits('update:minSalary', v)
  },
})
const maxSalary = computed({
  get: () => props.maxSalary,
  set: (v) => {
    if (!v) {
      return
    }
    emits('update:maxSalary', v)
  },
})
watch(miniSalary, (v) => {
  if (!v || !maxSalary.value) {
    return
  }
  if (v >= maxSalary.value) {
    maxSalary.value = void 0
  }
})
watch(_fixed, () => (maxSalary.value = void 0))
</script>

<template>
<VRow :dense="true">
  <VCol cols="4">
    <VSwitch v-model="_fixed" color="primary" label="固定薪资" />
  </VCol>
  <VCol :cols="props.fixed ? 8 : 4">
    <VCombobox
      v-model.number="miniSalary"
      autoSelectFirst="exact"
      :label="props.fixed ? `固定月薪` : `最小月薪`"
      :items="salaryRange"
      :errorMessages="props.errorMessages"
      :hint="props.hint"
      :persistentHint="props.persistentHint"
    />
  </VCol>
  <VCol v-if="!props.fixed" cols="4">
    <VCombobox
      v-model.number="maxSalary"
      autoSelectFirst="exact"
      label="最大月薪"
      :items="salaryRange.filter(e => e > miniSalary!)"
      :errorMessages="props.errorMessages"
      :hint="props.hint"
      :persistentHint="props.persistentHint"
    />
  </VCol>
</VRow>
</template>
